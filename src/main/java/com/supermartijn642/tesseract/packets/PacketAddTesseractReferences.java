package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.manager.TesseractReference;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created 14/04/2023 by SuperMartijn642
 */
public class PacketAddTesseractReferences implements BasePacket {

    private Collection<TesseractReference> references;
    private boolean clear;

    public PacketAddTesseractReferences(Collection<TesseractReference> references, boolean clearExisting){
        this.references = references;
        this.clear = clearExisting;
    }

    public PacketAddTesseractReferences(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeBoolean(this.clear);
        buffer.writeInt(this.references.size());
        for(TesseractReference reference : this.references)
            buffer.writeCompoundTag(reference.write());
    }

    @Override
    public void read(PacketBuffer buffer){
        this.clear = buffer.readBoolean();
        int size = buffer.readInt();
        this.references = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            try{
                TesseractReference reference = new TesseractReference(0, buffer.readCompoundTag(), true);
                this.references.add(reference);
            }catch(Exception e){
                throw new RuntimeException("Received invalid tesseract reference data!", e);
            }
        }
    }

    @Override
    public void handle(PacketContext context){
        if(this.clear)
            TesseractTracker.CLIENT.clear();
        for(TesseractReference reference : this.references)
            TesseractTracker.CLIENT.add(reference);
    }
}
