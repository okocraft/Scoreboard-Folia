package net.okocraft.scoreboard.display.board;

import io.netty.buffer.Unpooled;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.okocraft.scoreboard.ScoreboardPlugin;
import net.okocraft.scoreboard.board.Board;
import net.okocraft.scoreboard.display.line.LineDisplay;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PacketBasedBoardDisplay implements BoardDisplay {

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
        var setScorePackets = new ArrayList<ClientboundSetScorePacket>();
        var buf = new FriendlyByteBuf(Unpooled.buffer());

        for (int i = 0, lineSize = lines.size(); i < lineSize; i++) {
            var line = lines.get(i);
            var entryName = ENTRY_NAMES.get(i);

            buf.writeUtf(line.getName());
            buf.writeByte(0);
            teamParameters(buf, line);
            buf.writeCollection(List.of(entryName), FriendlyByteBuf::writeUtf);
            player.getHandle().connection.send(new ClientboundSetPlayerTeamPacket(buf));

            buf.clear();
            buf.writeUtf(entryName);
            buf.writeEnum(ServerScoreboard.Method.CHANGE);
            buf.writeUtf("sb");
            buf.writeVarInt(lineSize - i);

            setScorePackets.add(new ClientboundSetScorePacket(buf));
        }

        buf.clear();
        buf.writeUtf("sb");
        buf.writeByte(0);
        buf.writeComponent(title.getCurrentLine());
        buf.writeEnum(ObjectiveCriteria.RenderType.INTEGER);

        player.getHandle().connection.send(new ClientboundSetObjectivePacket(buf));

        buf.clear();
        buf.writeByte(1);
        buf.writeUtf("sb");
        player.getHandle().connection.send(new ClientboundSetDisplayObjectivePacket(buf));

        setScorePackets.forEach(player.getHandle().connection::send);
    }

    private void teamParameters(@NotNull FriendlyByteBuf buf, @NotNull LineDisplay lineDisplay) {
        buf.writeComponent(net.minecraft.network.chat.Component.literal(lineDisplay.getName()));
        buf.writeByte(3);
        buf.writeUtf(Team.Visibility.ALWAYS.name);
        buf.writeUtf(Team.CollisionRule.ALWAYS.name);
        buf.writeEnum(ChatFormatting.RESET);
        buf.writeComponent(lineDisplay.getCurrentLine());
        buf.writeComponent(CommonComponents.EMPTY);
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

        buf.writeUtf("sb");
        buf.writeByte(ClientboundSetObjectivePacket.METHOD_REMOVE);

        player.getHandle().connection.send(new ClientboundSetObjectivePacket(buf));

        for (var line : lines) {
            buf.clear();
            buf.writeUtf(line.getName());
            buf.writeByte(1);

            player.getHandle().connection.send(new ClientboundSetPlayerTeamPacket(buf));
        }
    }

    @Override
    public void applyTitle() {
        if (!title.isChanged()) {
            return;
        }

        var buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeUtf("sb");
        buf.writeByte(2);
        buf.writeComponent(title.getCurrentLine());
        buf.writeEnum(ObjectiveCriteria.RenderType.INTEGER);

        player.getHandle().connection.send(new ClientboundSetObjectivePacket(buf));
    }

    @Override
    public void applyLine(@NotNull LineDisplay line) {
        if (!line.isChanged()) {
            return;
        }

        var buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeUtf(line.getName());
        buf.writeByte(2);
        teamParameters(buf, line);

        player.getHandle().connection.send(new ClientboundSetPlayerTeamPacket(buf));
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
