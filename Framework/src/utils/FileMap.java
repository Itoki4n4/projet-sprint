package mg.itu.utils;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

import java.util.Set;

public class FileMap {
    String fileName;
    byte[] filebody;

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public byte[] getFilebody() {
        return filebody;
    }
    public void setFilebody(byte[] filebody) {
        this.filebody = filebody;
    }
    public FileMap(){}
    public FileMap(String fileName,byte[] b){
        this.setFileName(fileName);
        this.setFilebody(b);
    }
    public int saveFile(String filePath){
        int value = 0;
        FileOutputStream sortie = null;
        try{
            File file = new File(filePath + File.separatorChar + fileName);
            sortie = new FileOutputStream(file);
            sortie.write(this.getFilebody());
            System.out.println("Success");
            // Définir les permissions POSIX (par exemple : rwxrwxrwx)
            // Path path = Paths.get(file.getAbsolutePath());
            // Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            // Files.setPosixFilePermissions(path, permissions);
            return 1;
        }catch(IOException e){
            e.printStackTrace();
            value = 1;
        } finally{
            try{
                sortie.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return value;
    }
}
