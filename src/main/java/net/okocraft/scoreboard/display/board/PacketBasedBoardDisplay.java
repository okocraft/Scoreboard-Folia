package net.okocraft.scoreboard.display.board;

import io.netty.buffer.Unpooled;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.display.line.LineDisplay;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PacketBasedBoardDisplay implements BoardDisplay {

    private static final String OBJECTIVE_NAME = "sb";

    private final List<ScheduledTask> updateTasks = new ArrayList<>(17);

    private final Object lock = new Object();
    private volatile boolean visible = false;

    private final LineDisplay title;
    private final List<LineDisplay> lines;
    private final CraftPlayer player;
    private final boolean isBedrockEdition;

    public PacketBasedBoardDisplay(@NotNull Player player, @NotNull Board board) {
        if (player instanceof CraftPlayer craftPlayer) {
            this.player = craftPlayer;
            this.isBedrockEdition = player.getUniqueId().toString().startsWith("00000000");
        } else {
            throw new IllegalArgumentException(player + " is not CraftPlayer");
        }

        this.title = new LineDisplay(player, board.getTitle(), 0);

        int size = Math.min(board.getLines().size(), 16);
        var lines = new ArrayList<LineDisplay>(size);

        for (int i = 0; i < size; i++) {
            LineDisplay line = new LineDisplay(player, board.getLines().get(i), i);
            lines.add(line);
        }

        this.lines = Collections.unmodifiableList(lines);
    }

    @Override
    public @NotNull LineDisplay getTitle() {
        return title;
    }

    @Override
    public @NotNull List<LineDisplay> getLines() {
        return lines;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void showBoard() {
        synchronized (lock) {
            if (!visible) {
                sendShowPackets();
                scheduleUpdateTasks();
                visible = true;
            }
        }
    }

    private void sendShowPackets() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        // ClientboundSetObjectivePacket(FriendlyByteBuf)
        buf.writeUtf(OBJECTIVE_NAME); // objective name
        buf.writeByte(ClientboundSetObjectivePacket.METHOD_ADD); // method
        buf.writeComponent(title.getCurrentLine()); // display name
        buf.writeEnum(ObjectiveCriteria.RenderType.INTEGER); // render type
        buf.writeNullable(BlankFormat.INSTANCE, NumberFormatTypes::writeToStream); // number format

        player.getHandle().connection.send(new ClientboundSetObjectivePacket(buf));

        var setScorePackets = new ArrayList<ClientboundSetScorePacket>(this.lines.size());

        for (int i = 0, lineSize = lines.size(); i < lineSize; i++) {
            var line = lines.get(i);

            if (this.isBedrockEdition) { // Bedrock Edition does not support rendering the display name of score
                var entryName = ENTRY_NAMES.get(i);

                buf.clear();
                buf.writeUtf(line.getName()); // team name
                buf.writeByte(0); // method: ADD
                teamParameters(buf, line); // parameters
                buf.writeCollection(List.of(entryName), FriendlyByteBuf::writeUtf); // players
                this.player.getHandle().connection.send(new ClientboundSetPlayerTeamPacket(buf));

                setScorePackets.add(createSetScorePacket(entryName, lineSize - i, null));
            } else {
                setScorePackets.add(createSetScorePacket(line.getName(), lineSize - i, line.getCurrentLine()));
            }
        }

        buf.clear();

        // ClientboundSetDisplayObjectivePacket(FriendlyByteBuf)
        buf.writeByte(DisplaySlot.SIDEBAR.id());
        buf.writeUtf(OBJECTIVE_NAME);
        player.getHandle().connection.send(new ClientboundSetDisplayObjectivePacket(buf));

        setScorePackets.forEach(player.getHandle().connection::send);
    }

    @NotNull
    private static ClientboundSetScorePacket createSetScorePacket(String name, int score, Component component) {
        return new ClientboundSetScorePacket(
                name,
                OBJECTIVE_NAME,
                score,
                component != null ? PaperAdventure.asVanilla(component) : null,
                null
        );
    }

    private static void teamParameters(@NotNull FriendlyByteBuf buf, @NotNull LineDisplay lineDisplay) {
        buf.writeComponent(net.minecraft.network.chat.Component.empty()); // display name
        buf.writeByte(3); // options
        buf.writeUtf(Team.Visibility.ALWAYS.name);
        buf.writeUtf(Team.CollisionRule.ALWAYS.name);
        buf.writeEnum(ChatFormatting.RESET);
        buf.writeComponent(lineDisplay.getCurrentLine()); // prefix
        buf.writeComponent(CommonComponents.EMPTY); // suffix
    }

    @Override
    public void hideBoard() {
        synchronized (lock) {
            if (visible) {
                sendHidePacket();
                cancelUpdateTasks();
                visible = false;
            }
        }
    }

    private void sendHidePacket() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        // ClientboundSetObjectivePacket(FriendlyByteBuf)
        buf.writeUtf(OBJECTIVE_NAME); // objective name
        buf.writeByte(ClientboundSetObjectivePacket.METHOD_REMOVE); // method

        player.getHandle().connection.send(new ClientboundSetObjectivePacket(buf));

        if (this.isBedrockEdition) {
            for (var line : lines) {
                buf.clear();
                buf.writeUtf(line.getName()); // entry name
                buf.writeByte(1); // method: remove

                player.getHandle().connection.send(new ClientboundSetPlayerTeamPacket(buf));
            }
        }
    }

    @Override
    public void applyTitle() {
        if (!title.isChanged()) {
            return;
        }

        var buf = new FriendlyByteBuf(Unpooled.buffer());

        // ClientboundSetObjectivePacket(FriendlyByteBuf)
        buf.writeUtf(OBJECTIVE_NAME); // objective name
        buf.writeByte(ClientboundSetObjectivePacket.METHOD_CHANGE); // method
        buf.writeComponent(title.getCurrentLine()); // display name
        buf.writeEnum(ObjectiveCriteria.RenderType.INTEGER); // render type
        buf.writeNullable(BlankFormat.INSTANCE, NumberFormatTypes::writeToStream); // number format

        player.getHandle().connection.send(new ClientboundSetObjectivePacket(buf));
    }

    @Override
    public void applyLine(@NotNull LineDisplay line) {
        if (!line.isChanged()) {
            return;
        }

        if (this.isBedrockEdition) {
            var buf = new FriendlyByteBuf(Unpooled.buffer());

            buf.writeUtf(line.getName()); // team name
            buf.writeByte(2); // method: modify
            teamParameters(buf, line); // parameters

            player.getHandle().connection.send(new ClientboundSetPlayerTeamPacket(buf));
        } else {
            player.getHandle().connection.send(createSetScorePacket(line.getName(), lines.size() - line.getLineNumber(), line.getCurrentLine()));
        }
    }

    private void scheduleUpdateTasks() {
        if (getTitle().shouldUpdate()) {
            updateTasks.add(scheduleUpdateTask(getTitle(), true, getTitle().getInterval()));
        }

        for (LineDisplay line : getLines()) {
            if (line.shouldUpdate()) {
                updateTasks.add(scheduleUpdateTask(line, false, line.getInterval()));
            }
        }
    }

    private void cancelUpdateTasks() {
        updateTasks.stream().filter(Objects::nonNull).filter(t -> !t.isCancelled()).forEach(ScheduledTask::cancel);
        updateTasks.clear();
    }

    private ScheduledTask scheduleUpdateTask(@NotNull LineDisplay display, boolean isTitleLine, long interval) {
        return player.getScheduler().runAtFixedRate(ScoreboardPlugin.getPlugin(), $ -> update(display, isTitleLine), null, interval, interval);
    }

    private void update(@NotNull LineDisplay display, boolean isTitleLine) {
        if (!visible) {
            return;
        }

        display.update();

        if (isTitleLine) {
            this.applyTitle();
        } else {
            this.applyLine(display);
        }
    }
}
