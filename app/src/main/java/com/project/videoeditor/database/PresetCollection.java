package com.project.videoeditor.database;

import java.util.ArrayList;
import java.util.List;


public class PresetCollection {
    private ArrayList<PresetEntity> collection;

    public PresetCollection(List<PresetEntity> collection) {
        this.collection = (ArrayList<PresetEntity>) collection;
    }

    public List<String> getAllNameFormat()
    {
        ArrayList<String> nameFormatArray = new ArrayList<String>();
        for(int i = 0;i < collection.size();i++)
        {
            nameFormatArray.add(collection.get(i).getNameFormat());
        }
        return nameFormatArray;
    }

    public List<String> getAllResolutionVideo()
    {
        ArrayList<String> nameFormatArray = new ArrayList<String>();
        for(int i = 0;i < collection.size();i++)
        {
            nameFormatArray.add(collection.get(i).getPixelResolution());
        }
        return nameFormatArray;
    }

    public List<String> getAllAspectRatio()
    {
        ArrayList<String> nameFormatArray = new ArrayList<String>();
        for(int i = 0;i < collection.size();i++)
        {
            nameFormatArray.add(collection.get(i).getImageProportion());
        }
        return nameFormatArray;
    }

    public List<Float> getAllCBRValue()
    {
        ArrayList<Float> nameFormatArray = new ArrayList<Float>();
        for(int i = 0;i < collection.size();i++)
        {
            nameFormatArray.add(collection.get(i).getCBR());
        }
        return nameFormatArray;
    }

    public List<Float> getAllMinCBRValue()
    {
        ArrayList<Float> nameFormatArray = new ArrayList<Float>();
        for(int i = 0;i < collection.size();i++)
        {
            nameFormatArray.add(collection.get(i).getMinimumBitrate());
        }
        return nameFormatArray;
    }

    public List<Float> getAllMaxCBRValue()
    {
        ArrayList<Float> nameFormatArray = new ArrayList<Float>();
        for(int i = 0;i < collection.size();i++)
        {
            nameFormatArray.add(collection.get(i).getMaximumBitrate());
        }
        return nameFormatArray;
    }
    public PresetEntity getById(int id)
    {
        return collection.get(id);
    }

    public PresetEntity getByNameFormat(String nameFormat){
        for (PresetEntity elem : collection)
        {
            if(elem.getNameFormat().equals(nameFormat))
                return elem;
        }
        return null;
    }
}
