package datastructures;

import java.io.*;
import java.util.ArrayList;

public class Directory {
    private File file;
    private RandomAccessFile fileReader;

    private static int p;
    ArrayList<Long> dir = new ArrayList<>(); // actual directory

    public Directory(String F) throws IOException {
        this.file = new File(F); // creates the "file" file
        fileReader = new RandomAccessFile(file, "rw"); // opens the file in read and write mode
        if (fileReader.length() > 0) {
            loadDirectory();
            p = fileReader.readInt();
        } else {
            p = 1;
        }
        for (int i = 0; i < (int) Math.pow(2, p); i++) {
            try {
                dir.get(i);
            } catch (Exception e) {
                dir.add(i, (long) -1);
            }
        }
    }

    public int hashFunc(Key item) {
        int id = item.getId();
        return id % (int) Math.pow(2, p); // returns k mod 2p
    }

    public int hashFunc(int id) {
        return (int) (id % (Math.pow(2, p))); // returns k mod 2p
    }

    public int bitExtracted(int number, int p) {
        return (((1 << p) - 1) & (number >> 0)); // returns the p significant bits of number in integer format
    }

    public void AddItem(int id, long address) throws IOException {
        Key item = new Key(id, address); // creates key from info provided
        int hashFuncValue = hashFunc(item); // gets the hash function value for id of item

        int pos = bitExtracted(hashFuncValue, p); // witch bucket
        Bucket b;
        try {
            b = readBucket(dir.get(pos)); // if bucket already exists it sets b with bucket info
        } catch (Exception e) {
            b = new Bucket(p, fileReader.length(), file); // creates bucket in the end of file
            dir.remove(pos);
            dir.add(pos, b.getAddress());// add address to hash table
        }

        boolean tryAdd = b.AddKey(item);
        if (tryAdd) { // if item was added correctly writes bucket in file
            b.WriteFile();
        } else { // AddKey failled and Rehash is needed
            pos = bitExtracted(hashFuncValue, b.getP());
            Rehash(pos, b.getKeys(), b, item);
        }

    }

    private void Rehash(int pos, ArrayList<Key> keys, Bucket b, Key item) throws IOException {
        int newPos = pos;
        int posKey;
        Bucket b2;
        ArrayList<Key> k1 = new ArrayList<Key>();
        ArrayList<Key> k2 = new ArrayList<Key>();

        b.setP(b.getP() + 1);

        if (b.getP() > p) {
            p = b.getP(); // increases the directory depth
            for (int i = 0; i < (int) Math.pow(2, p); i++) {
                try {
                    dir.get(i);
                } catch (Exception e) {
                    int bit = bitExtracted(i, p - 1);
                    dir.add(i, dir.get(bit));
                    if (bit == pos)
                        newPos = i;
                }
            }
        }

        for (Key key : keys) {
            posKey = hashFunc(key); // redos the hash function for each of the buckets items
            if (pos == posKey) {
                k1.add(key);
            } else {
                k2.add(key);
                newPos = bitExtracted(posKey, b.getP());
            }
        }

        // adds the new item to correct bucket
        posKey = hashFunc(item);
        if (pos == posKey) {
            k1.add(item);
        } else {
            k2.add(item);
            newPos = bitExtracted(posKey, b.getP());
        }
        b.Resetbucket(k1);

        // if second bucket was created adds it to directory
        if (k2.size() > 0) {
            b2 = new Bucket(b.getP(), fileReader.length(), file);
            b2.Resetbucket(k2);
            dir.remove(newPos);
            dir.add(newPos, b2.getAddress());
        }
        posKey++;
    }

    public Bucket readBucket(long address) throws IOException {
        Bucket b = new Bucket(p, address, file);
        b.readFile(address); // sets b with info from that address

        return b;
    }

    public long search(int id) throws FileNotFoundException {
        int hashFuncValue = hashFunc(id);
        int pos = bitExtracted(hashFuncValue, p); // witch bucket
        long add = -1;
        Bucket b;
        try {
            b = readBucket(dir.get(pos)); // see if bucket exists
            if (b.getP() < p) {
                pos = bitExtracted(hashFuncValue, b.getP());
                b = readBucket(dir.get(pos)); // see if bucket exists
            }

            add = b.getAddress(); // gets bucket address
            if (add != -1) {
                long addItem = b.search(id);
                if (addItem != -1)
                    return addItem; // searches in that bucket for id
            }
        } catch (Exception e) {
            // System.out.print(e.toString());
            pos = bitExtracted(hashFuncValue, (p - 1));
            try {
                b = readBucket(dir.get(pos));
                add = b.getAddress(); // gets bucket address
                if (add != -1) {
                    long addItem = b.search(id);
                    if (addItem != -1)
                        return addItem; // searches in that bucket for id
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } // see if bucket exists

        }
        return -1; // if not found return an offBounds value
    }

    public void clear() {
        if (file.length() > 0) {
            file.delete(); // deletes file
        }
    }

    // reads and prints all of the buckets
    public void readFile() throws IOException {
        fileReader.seek(0);
        while (fileReader.getFilePointer() < fileReader.length()) {
            System.out.println("p" + fileReader.readInt());

            fileReader.readInt();
            int count = 0;

            long position = fileReader.getFilePointer();
            while (count < 68 && fileReader.getFilePointer() < fileReader.length()) {
                fileReader.seek(position);
                System.out.println("id = " + fileReader.readInt());
                System.out.println("address = " + fileReader.readLong());
                position = fileReader.getFilePointer();
                count++;
            }
        }
    }

    public void saveDirectory() throws IOException {
        File dirFile = new File("dir.db");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(dirFile))) {
            out.writeInt(p);
            out.writeInt(dir.size());
            for (Long address : dir) {
                out.writeLong(address);
            }
        }
    }

    public void loadDirectory() throws IOException {
        File dirFile = new File("dir.db");
        if (dirFile.length() > 0) {
            try (DataInputStream in = new DataInputStream(new FileInputStream(dirFile))) {
                p = in.readInt();
                int size = in.readInt();
                dir = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    dir.add(in.readLong());
                }
            }
        }
    }

    public void deleteItem(int id) throws IOException {
        int hashFuncValue = hashFunc(id);
        int pos = bitExtracted(hashFuncValue, p); // which bucket

        try {
            Bucket b = readBucket(dir.get(pos));
            if (b.getP() < p) {
                pos = bitExtracted(hashFuncValue, b.getP());
                b = readBucket(dir.get(pos));
            }

            long bucketAddress = b.getAddress();
            if (bucketAddress != -1) {
                b.deleteKey(id);
                b.WriteFile(); // Update the bucket in the file
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateItem(int id, long newAddress) throws IOException {
        int hashFuncValue = hashFunc(id);
        int pos = bitExtracted(hashFuncValue, p); // which bucket

        try {
            Bucket b = readBucket(dir.get(pos));
            if (b.getP() < p) {
                pos = bitExtracted(hashFuncValue, b.getP());
                b = readBucket(dir.get(pos));
            }

            long bucketAddress = b.getAddress();
            if (bucketAddress != -1) {
                b.updateKey(id, newAddress);
                b.WriteFile(); // Update the bucket in the file
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}