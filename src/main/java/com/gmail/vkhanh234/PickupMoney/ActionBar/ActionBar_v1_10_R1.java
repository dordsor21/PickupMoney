package com.gmail.vkhanh234.PickupMoney.ActionBar;

import net.minecraft.server.v1_10_R1.IChatBaseComponent;
import net.minecraft.server.v1_10_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBar_v1_10_R1 implements ActionBar {

	public void send(Player p, String s)
	{
		IChatBaseComponent barmsg = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + s + "\"}");
		PacketPlayOutChat bar = new PacketPlayOutChat(barmsg, (byte)2);
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(bar);
	}
}
