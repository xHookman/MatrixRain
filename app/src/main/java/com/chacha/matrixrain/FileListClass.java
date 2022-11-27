package com.chacha.matrixrain;

import java.io.File;
import java.util.ArrayList;

public class FileListClass {

    public File[] GetFiles(String DirectoryPath) {
        File f = new File(DirectoryPath);
        f.mkdirs();
        return f.listFiles();
    }

    public ArrayList<String> getFileNames(File[] file){
        ArrayList<String> arrayFiles = new ArrayList<>();
        if (file.length == 0)
            return null;
        else {
            for (File value : file) arrayFiles.add(value.getName());
        }

        return arrayFiles;
    }


}
