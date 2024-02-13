package com.supermartijn642.tesseract.manager;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created 4/22/2020 by SuperMartijn642
 */
public class ChannelList {

    private int channelId = 0;

    public final EnumChannelType type;
    private final List<Channel> channels = new ArrayList<>();
    private final List<Channel> publicChannels = new ArrayList<>();
    private final Map<Integer,Channel> channelsById = new HashMap<>();
    private final Map<UUID,List<Channel>> channelsByCreator = new HashMap<>();
    private final List<Integer> removedIds = new ArrayList<>();

    public ChannelList(EnumChannelType type){
        this.type = type;
    }

    public Channel add(UUID creator, boolean isPrivate, String name){
        Channel channel = new Channel(this.channelId++, this.type, creator, isPrivate, name);
        this.add(channel);
        return channel;
    }

    void add(Channel channel){
        this.channels.add(channel);
        if(!channel.isPrivate)
            this.publicChannels.add(channel);
        this.channelsById.put(channel.id, channel);
        this.channelsByCreator.putIfAbsent(channel.creator, new ArrayList<>());
        this.channelsByCreator.get(channel.creator).add(channel);
    }

    public void remove(int id){
        this.remove(this.getById(id));
    }

    public void remove(Channel channel){
        if(channel == null)
            return;

        this.channels.remove(channel);
        this.publicChannels.remove(channel);
        this.channelsById.remove(channel.id);
        this.channelsByCreator.get(channel.creator).remove(channel);
        channel.delete();
        this.removedIds.add(channel.id);
    }

    public List<Channel> sortForPlayer(Player player){
        final UUID uuid = player.getUUID();
        this.channels.sort((a, b) -> {
            boolean aUuid = a.creator.equals(uuid);
            boolean bUuid = b.creator.equals(uuid);
            if(aUuid ^ bUuid)
                return aUuid ? -1 : 1;
            return a.name.compareTo(b.name);
        });
        return this.channels;
    }

    public Channel getById(int id){
        return this.channelsById.get(id);
    }

    public List<Channel> getChannels(){
        return Collections.unmodifiableList(this.channels);
    }

    public List<Channel> getPublicChannels(){
        return Collections.unmodifiableList(this.publicChannels);
    }

    public List<Channel> getChannelsCreatedBy(UUID creator){
        return this.channelsByCreator.getOrDefault(creator, Collections.emptyList());
    }

    public void write(Path folder){
        for(Channel channel : this.channels){
            Path file = folder.resolve("channel" + channel.id + ".nbt");
            try(DataOutputStream output = new DataOutputStream(Files.newOutputStream(file))){
                NbtIo.write(channel.write(), output);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        for(int id : this.removedIds){
            Path file = folder.resolve("channel" + id + ".nbt");
            try{
                Files.deleteIfExists(file);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        this.removedIds.clear();
    }

    public void read(Path folder){
        this.channelId = 0;

        if(!Files.exists(folder))
            return;

        try(Stream<Path> files = Files.list(folder)){
            files.forEach(file -> {
                String name = file.getFileName().toString();
                if(name.startsWith("channel") && name.endsWith(".nbt")){
                    try{
                        int id = Integer.parseInt(name.substring("channel".length(), name.length() - ".nbt".length()));
                        CompoundTag compound;
                        try(DataInputStream input = new DataInputStream(Files.newInputStream(file))){
                            compound = NbtIo.read(input);
                        }
                        Channel channel = new Channel(id, this.type, compound);
                        if(channel.id >= this.channelId)
                            this.channelId = channel.id + 1;
                        this.add(channel);
                    }catch(Exception e){
                        Tesseract.LOGGER.error("Failed to read channel from file '" + file + "'!", e);
                    }
                }
            });
        }catch(IOException e){
            Tesseract.LOGGER.error("Failed to list files from '" + folder + "'!", e);
        }
    }

    public void clear(){
        this.channels.clear();
        this.publicChannels.clear();
        this.channelsById.clear();
        this.channelsByCreator.clear();
    }
}
