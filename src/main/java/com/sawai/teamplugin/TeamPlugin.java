package com.sawai.teamplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class TeamPlugin extends JavaPlugin implements CommandExecutor, Listener {

    private Scoreboard scoreboard;
    private Map<String, Team> playerTeamMap;

    @Override
    public void onEnable() {
        getCommand("team").setExecutor(this);
        scoreboard = getServer().getScoreboardManager().getMainScoreboard();
        playerTeamMap = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはプレイヤーのみ使用可能です。");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("team")) {
            if (args.length > 0) {
                String subCommand = args[0].toLowerCase();

                if (subCommand.equals("join")) {
                    handleTeamCommand(player, args);
                } else if (subCommand.equals("leave")) {
                    handleLeaveCommand(player);
                } else {
                    player.sendMessage(ChatColor.RED + "無効なサブコマンドです。使用法: /team join チーム名 プレイヤー名");
                }
            } else {
                player.sendMessage(ChatColor.RED + "使用法: /team join チーム名 プレイヤー名");
            }
        }

        return true;
    }

    private void handleTeamCommand(Player player, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("join")) {
            player.sendMessage(ChatColor.RED + "使用法: /team join チーム名 プレイヤー名");
            return;
        }

        String teamName = args[1].toLowerCase();
        String playerName = args[2];

        Team targetTeam = scoreboard.getTeam(teamName);

        if (targetTeam == null) {
            player.sendMessage(ChatColor.RED + "無効なチームです。");
            return;
        }

        // プレイヤーを指定のチームに追加
        targetTeam.addEntry(playerName);
        playerTeamMap.put(playerName, targetTeam);

        player.sendMessage(ChatColor.GREEN + playerName + "をチームに追加しました: " + targetTeam.getPrefix());
    }

    private void handleLeaveCommand(Player player) {
        if (playerTeamMap.containsKey(player.getName())) {
            Team playerTeam = playerTeamMap.get(player.getName());

            // プレイヤーをチームから削除
            playerTeam.removeEntry(player.getName());
            playerTeamMap.remove(player.getName());

            player.sendMessage(ChatColor.YELLOW + "チームから退出しました。");
        } else {
            player.sendMessage(ChatColor.RED + "あなたはどのチームにも所属していません。");
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (playerTeamMap.containsKey(player.getName())) {
            Team playerTeam = playerTeamMap.get(player.getName());

            // チームチャットのメッセージに変更
            event.setFormat(ChatColor.GRAY + "[" + playerTeam.getPrefix() + "TeamChat" + ChatColor.GRAY + "] "
                    + player.getDisplayName() + ": " + message);

            // チームメンバーにメッセージを送信
            playerTeam.getEntries().forEach(member -> {
                Player teamMember = getServer().getPlayerExact(member);
                if (teamMember != null) {
                    teamMember.sendMessage(event.getFormat());
                }
            });
        }
    }
}

