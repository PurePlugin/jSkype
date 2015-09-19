package xyz.gghost.jskype.internal.impl;

import lombok.Getter;
import lombok.Setter;
import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.message.Message;
import xyz.gghost.jskype.internal.message.MessageHistory;
import xyz.gghost.jskype.internal.packet.packets.PingPrepPacket;
import xyz.gghost.jskype.internal.packet.packets.SendMessagePacket;
import xyz.gghost.jskype.internal.packet.packets.UserManagementPacket;
import xyz.gghost.jskype.internal.user.GroupUser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ghost on 19/09/2015.
 */
public class GroupImpl implements Group {

    @Setter @Getter private String topic;
    @Setter @Getter private String id;
    @Setter @Getter private String pictureUrl;
    private SkypeAPI api;

    public MessageHistory getMessageHistory(){
        if(api.getA().containsKey(id))
            return api.getA().get(id);
        MessageHistory history = new MessageHistory(id, api);
        api.getA().put(id, history);
        return history;
    }

    @Getter private List<GroupUser> clients = new ArrayList<GroupUser>();
    public GroupImpl(SkypeAPI api, String longId){
        this.id = longId;
        this.api = api;
    }
    public void kick(String usr) {
        new UserManagementPacket(api).kickUser(getId(), usr);
    }

    public void add(String usr) {
        new UserManagementPacket(api).addUser(getId(), usr);
    }

    public String getId() {
        return id.split("@")[0].split(":")[1];
    }

    public String getLongId() {
        return id;
    }

    public Message sendMessage(Message msg) {
        return new SendMessagePacket(api).sendMessage(id, msg);
    }
    public Message sendMessage(String msg) {
        return new SendMessagePacket(api).sendMessage(id, new Message(msg));
    }
    public Message sendImage(File url) {
        return new SendMessagePacket(api).sendPing(id, new Message(""), new PingPrepPacket(api).urlToId(url, id));
    }

    public Message sendImage(URL url) {
        return new SendMessagePacket(api).sendPing(id, new Message(""), new PingPrepPacket(api).urlToId(url.toString(), id));
    }
    public List<GroupUser> getClients() {
        return clients;
    }
    public String getTopic() {
        return topic;
    }
    public boolean isUserChat(){
        return !getLongId().contains("19:");
    }
    public void leave(){
        kick(api.getUsername());
    }
}
