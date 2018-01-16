package com.gmail.vkhanh234.PickupMoney.ActionBar;

import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBar_v1_9_R2 implements ActionBar {

	public void send(Player p, String s)
	{
		IChatBaseComponent barmsg = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + s + "\"}");
		PacketPlayOutChat bar = new PacketPlayOutChat(barmsg, (byte)2);
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(bar);
	}
}
