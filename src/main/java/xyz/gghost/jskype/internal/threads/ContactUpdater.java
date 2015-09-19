package xyz.gghost.jskype.internal.threads;

import xyz.gghost.jskype.Group;
import xyz.gghost.jskype.SkypeAPI;
import xyz.gghost.jskype.internal.impl.ContactGroupImpl;
import xyz.gghost.jskype.internal.packet.packets.GetContactsPacket;

public class ContactUpdater extends Thread {

    private final SkypeAPI api;

    public ContactUpdater(SkypeAPI api) {
        this.api = api;
    }
    @Override
    public void run() {
        while (this.isAlive()) {
            try {
                new GetContactsPacket(api).setupContact();
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {} catch (Exception e) {
                e.printStackTrace();
            }
        }
        Group group = null;
    }
}