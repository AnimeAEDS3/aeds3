package datastructures;

import java.io.*;
import java.util.*;

public class InvertedListName implements Serializable {
    private Map<String, List<Key>> invertedIndex;

    public InvertedListName() throws IOException {
        invertedIndex = new HashMap<>();
        RandomAccessFile raf = new RandomAccessFile("invertedListName.db","rw");
        if (raf.length() > 0) {
            try {
                InvertedListName loadedList = loadFromFile();
                this.invertedIndex = loadedList.invertedIndex;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading InvertedListName from file: " + e.getMessage());
            }
        }
    }
    

    public void addKey(String animeName, Key key) {
        String[] terms = animeName.toLowerCase().split("\\s+");
        for (String term : terms) {
            invertedIndex.computeIfAbsent(term, k -> new ArrayList<>()).add(key);
        }
    }

    public List<Key> getKeys(String term) {
        return invertedIndex.getOrDefault(term.toLowerCase(), new ArrayList<>());
    }

    public void removeKey(String animeName, Key key) {
        String[] terms = animeName.toLowerCase().split("\\s+");
        for (String term : terms) {
            invertedIndex.computeIfPresent(term, (k, v) -> {
                v.remove(key);
                return v.isEmpty() ? null : v;
            });
        }
    }

    public void updateKey(String animeName, Key oldKey, Key newKey) {
        String[] terms = animeName.toLowerCase().split("\\s+");
        for (String term : terms) {
            if (invertedIndex.containsKey(term)) {
                List<Key> keys = invertedIndex.get(term);
                int index = keys.indexOf(oldKey);
                if (index != -1) {
                    keys.set(index, newKey);
                }
            }
        }
    }

    public void saveToFile() throws IOException {
        File file = new File("invertedListName.db");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }
    
    public static InvertedListName loadFromFile() throws IOException, ClassNotFoundException {
        File file = new File("invertedListName.db");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (InvertedListName) ois.readObject();
        }
    }
}