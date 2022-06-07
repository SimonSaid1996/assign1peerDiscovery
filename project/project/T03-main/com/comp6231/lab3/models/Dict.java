package com.comp6231.lab3.models;

import java.util.ArrayList;
import java.util.HashMap;

public class Dict {
    private HashMap<String, ArrayList<Integer>> dict = new HashMap<>();

    public boolean addValue(String key,Integer value){
        if (dict.containsKey(key)){
            dict.get(key).add(value);
            return true;
        }
        return false;
    }

    public boolean set(String key,ArrayList<Integer> values){
        if (dict.containsKey(key)){
            dict.remove(key);
        }
        dict.put(key,values);
        return true;
    }


    public boolean delete(String key,Integer value){
        if (!dict.containsKey(key)){
            return false;
        }
        if(dict.get(key).remove(value)) {
            return true;
        }
        return false;
    }
    public boolean delete(String key){
        if (!dict.containsKey(key)){
            return false;
        }
        dict.remove(key);
        return true;
    }
    public ArrayList<String> list_keys(){
        return new ArrayList<String>(dict.keySet());
    }

    public Integer getValue(String key){
        if (!dict.containsKey(key)){
            return null;
        }
        return dict.get(key).get(0);
    }

    public ArrayList<Integer> getValues(String key){
        if (!dict.containsKey(key)){
            return null;
        }
        return dict.get(key);
    }

    public Integer sum(String key){
        ArrayList<Integer> values=dict.get(key);
        Integer sum=0;
        for(int i=0 ; i<values.size(); i+=1 ){
            sum+=values.get(i);
        }
        return sum;
    }
    public Integer max(String key){   //new added, ask the prof if the number have to be positive
        ArrayList<Integer> values=dict.get(key);
        Integer max=values.get(0);
        for(int i=1 ; i<values.size(); i+=1 ){
            if(max < values.get(i))
                max = values.get(i);
        }
        return max;
    }

    public Integer min(String key){  //new added
        ArrayList<Integer> values=dict.get(key);
        Integer min=values.get(0);
        for(int i=1 ; i<values.size(); i+=1 ){
            if(min > values.get(i))
                min = values.get(i);
        }
        return min;
    }

    public void reset(){
        dict.clear();
    }
    public static void main(String[] args) {
        Dict d=new Dict();
        ArrayList<Integer> a = new ArrayList<>();
        a.add(12);
        a.add(13);
        d.set("a",a);
        d.addValue("a",15);
        System.out.println("out "+d.getValue("a"));
        System.out.println("out "+d.getValues("a"));
    }
}
