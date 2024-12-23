package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.tesseract.Tesseract;

/**
 * Created 23/12/2024 by SuperMartijn642
 */
public class TesseractItemInfoGenerator extends ItemInfoGenerator {

    public TesseractItemInfoGenerator(ResourceCache cache){
        super("tesseract", cache);
    }

    @Override
    public void generate(){
        this.simpleInfo(Tesseract.tesseract, "item/tesseract");
    }
}
