package datastructures;

import java.io.*;
import java.util.*;

public class InvertedListGenre implements Serializable {
    private Map<String, List<Key>> invertedIndex;

    public InvertedListGenre() throws IOException {
        invertedIndex = new HashMap<>();
        RandomAccessFile raf = new RandomAccessFile("invertedListGenre.db", "rw");
        if (raf.length() > 0) {
            try {
                InvertedListGenre loadedList = loadFromFile();
                this.invertedIndex = loadedList.invertedIndex;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading InvertedListGenre from file: " + e.getMessage());
            }
        }
    }

    public void addKey(String[] genres, Key key) {
        for (String genre : genres) {
            String[] terms = genre.toLowerCase().split("\\s+");
            for (String term : terms) {
                invertedIndex.computeIfAbsent(term, k -> new ArrayList<>()).add(key);
            }
        }
    }

    public List<Key> getKeys(String[] terms) {
        List<Key> keys = new ArrayList<>();
        for (String term : terms) {
            for (Map.Entry<String, List<Key>> entry : invertedIndex.entrySet()) {
                if (entry.getKey().toLowerCase().contains(term.toLowerCase())) {
                    keys.addAll(entry.getValue());
                }
            }
        }
        return keys;
    }
    

    public void removeKey(String[] genres, Key key) {
        for (String genre : genres) {
            String[] terms = genre.toLowerCase().split("\\s+");
            for (String term : terms) {
                invertedIndex.computeIfPresent(term, (k, v) -> {
                    v.remove(key);
                    return v.isEmpty() ? null : v;
                });
            }
        }
    }

    public void updateKey(String[] genres, Key oldKey, Key newKey) {
        for (String genre : genres) {
            String[] terms = genre.toLowerCase().split("\\s+");
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
    }

    public void saveToFile() throws IOException {
        File file = new File("invertedListGenre.db");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    public static InvertedListGenre loadFromFile() throws IOException, ClassNotFoundException {
        File file = new File("invertedListGenre.db");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (InvertedListGenre) ois.readObject();
        }
    }
}