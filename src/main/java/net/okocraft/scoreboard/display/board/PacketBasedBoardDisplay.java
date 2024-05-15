package net.okocraft.scoreboard.display.board;

import io.netty.buffer.Unpooled;
import io.papermc.paper.adventure.AdventureCodecs;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.util.Tick;
import net.kyori.adventure.text.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.display.line.LineDisplay;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PacketBasedBoardDisplay implements BoardDisplay {

    private static final String OBJECTIVE_NAME = "sb";
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<NumberFormat> BLANK = Optional.of(BlankFormat.INSTANCE);
    private static final ClientboundSetObjectivePacket HIDE_PACKET;
    
    static {
        var buf = newByteBuf();

        // ClientboundSetObjectivePacket(RegistryFriendlyByteBuf)
        buf.writeUtf(OBJECTIVE_NAME); // objective name
        buf.writeByte(ClientboundSetObjectivePacket.METHOD_REMOVE); // method
        
        HIDE_PACKET = ClientboundSetObjectivePacket.STREAM_CODEC.decode(buf);
    }

    private final List<ScheduledTask> updateTasks = new ArrayList<>(17);

    private final Object lock = new Object();
    private volatile boolean visible = false;

    private final LineDisplay title;
    private final List<LineDisplay> lines;
    private final CraftPlayer player;

    public PacketBasedBoardDisplay(@NotNull Player player, @NotNull Board board) {
        if (player instanceof CraftPlayer craftPlayer) {
            this.player = craftPlayer;
        } else {
            throw new IllegalArgumentException(player + " is not CraftPlayer");
        }

        this.title = new LineDisplay(player, board.title(), 0);

        int size = Math.min(board.lines().size(), 16);
        var lines = new ArrayList<LineDisplay>(size);

        for (int i = 0; i < size; i++) {
            LineDisplay line = new LineDisplay(player, board.lines().get(i), i);
            lines.add(line);
        }

        this.lines = Collections.unmodifiableList(lines);
    }

    @Override
    public @NotNull LineDisplay getTitle() {
        return this.title;
    }

    @Override
    public @NotNull List<LineDisplay> getLines() {
        return this.lines;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void showBoard() {
        synchronized (this.lock) {
            if (!this.visible) {
                this.sendShowPackets();
                this.scheduleUpdateTasks();
                this.visible = true;
            }
        }
    }

    private void sendShowPackets() {
        var buf = newByteBuf();

        this.updateTitlePacket(buf, ClientboundSetObjectivePacket.METHOD_ADD);

        var setScorePackets = new ArrayList<ClientboundSetScorePacket>(this.lines.size());

        for (int i = 0, lineSize = this.lines.size(); i < lineSize; i++) {
            var line = this.lines.get(i);
            setScorePackets.add(createSetScorePacket(line.getName(), lineSize - i, line.getCurrentLine()));
        }

        buf.clear();

        // ClientboundSetDisplayObjectivePacket(RegistryFriendlyByteBuf)
        buf.writeByte(DisplaySlot.SIDEBAR.id());
        buf.writeUtf(OBJECTIVE_NAME);
        this.player.getHandle().connection.send(ClientboundSetDisplayObjectivePacket.STREAM_CODEC.decode(buf));

        setScorePackets.forEach(this.player.getHandle().connection::send);
    }

    private void updateTitlePacket(RegistryFriendlyByteBuf buf, int method) {
        // ClientboundSetObjectivePacket(RegistryFriendlyByteBuf)
        buf.writeUtf(OBJECTIVE_NAME);
        buf.writeByte(method);
        AdventureCodecs.STREAM_COMPONENT_CODEC.encode(buf, this.title.getCurrentLine()); // display name
        buf.writeEnum(ObjectiveCriteria.RenderType.INTEGER); // render type
        NumberFormatTypes.OPTIONAL_STREAM_CODEC.encode(buf, BLANK); // number format

        this.player.getHandle().connection.send(ClientboundSetObjectivePacket.STREAM_CODEC.decode(buf));
    }

    @NotNull
    private static ClientboundSetScorePacket createSetScorePacket(String name, int score, Component component) {
        return new ClientboundSetScorePacket(
                name,
                OBJECTIVE_NAME,
                score,
                Optional.ofNullable(component).map(PaperAdventure::asVanilla),
                Optional.empty()
        );
    }

    @Override
    public void hideBoard() {
        synchronized (this.lock) {
            if (this.visible) {
                this.sendHidePacket();
                this.cancelUpdateTasks();
                this.visible = false;
            }
        }
    }

    private void sendHidePacket() {
        this.player.getHandle().connection.send(HIDE_PACKET);
    }

    @Override
    public void applyTitle() {
        if (this.title.isChanged()) {
            this.updateTitlePacket(newByteBuf(), ClientboundSetObjectivePacket.METHOD_CHANGE);
        }
    }

    @Override
    public void applyLine(@NotNull LineDisplay line) {
        if (!line.isChanged()) {
            return;
        }
        this.player.getHandle().connection.send(createSetScorePacket(line.getName(), this.lines.size() - line.getLineNumber(), line.getCurrentLine()));
    }

    private void scheduleUpdateTasks() {
        if (this.getTitle().shouldUpdate()) {
            this.updateTasks.add(this.scheduleUpdateTask(this.getTitle(), true, this.getTitle().getInterval()));
        }

        for (LineDisplay line : this.getLines()) {
            if (line.shouldUpdate()) {
                this.updateTasks.add(this.scheduleUpdateTask(line, false, line.getInterval()));
            }
        }
    }

    private void cancelUpdateTasks() {
        this.updateTasks.stream().filter(Objects::nonNull).filter(t -> !t.isCancelled()).forEach(ScheduledTask::cancel);
        this.updateTasks.clear();
    }

    private ScheduledTask scheduleUpdateTask(@NotNull LineDisplay display, boolean isTitleLine, long interval) {
        var duration = Tick.of(interval);
        return Bukkit.getAsyncScheduler().runAtFixedRate(ScoreboardPlugin.getPlugin(), $ -> this.update(display, isTitleLine), duration.toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void update(@NotNull LineDisplay display, boolean isTitleLine) {
        if (!this.visible) {
            return;
        }

        display.update();

        if (isTitleLine) {
            this.applyTitle();
        } else {
            this.applyLine(display);
        }
    }

    private static RegistryFriendlyByteBuf newByteBuf() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), MinecraftServer.getServer().registryAccess());
    }
}
