/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.database.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utilities class to zip and unzip folders.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class CompressionUtilities {

    /**
     * Compress a folder and its contents.
     * 
     * @param srcFolder path to the folder to be compressed.
     * @param destZipFile path to the final output zip file.
     * @param addBaseFolder flag to decide whether to add also the provided base folder or not.
     * @throws IOException 
     */
    static public void zipFolder( String srcFolder, String destZipFile, boolean addBaseFolder )
            throws IOException {
        if (new File(srcFolder).isDirectory()) {

            ZipOutputStream zip = null;
            FileOutputStream fileWriter = null;
            try {
                fileWriter = new FileOutputStream(destZipFile);
                zip = new ZipOutputStream(fileWriter);
                addFolderToZip("", srcFolder, zip, addBaseFolder); //$NON-NLS-1$
            } finally {
                if (zip != null) {
                    zip.flush();
                    zip.close();
                }
                if (fileWriter != null)
                    fileWriter.close();
            }
        } else {
            throw new IOException(srcFolder + " is not a folder.");
        }
    }

    /**
     * Uncompress a compressed file to the contained structure.
     * 
     * @param zipFile the zip file that needs to be unzipped
     * @param destFolder the folder into which unzip the zip file and create the folder structure
     * @throws IOException 
     */
    public static void unzipFolder( String zipFile, String destFolder ) throws IOException {
        ZipFile zf = new ZipFile(zipFile);
        Enumeration< ? extends ZipEntry> zipEnum = zf.entries();
        String dir = destFolder;

        while( zipEnum.hasMoreElements() ) {
            ZipEntry item = (ZipEntry) zipEnum.nextElement();

            if (item.isDirectory()) {
                File newdir = new File(dir + File.separator + item.getName());
                if (!newdir.mkdir())
                    throw new IOException();
            } else {
                String newfilePath = dir + File.separator + item.getName();
                File newFile = new File(newfilePath);
                if (!newFile.getParentFile().exists()) {
                    if (!newFile.getParentFile().mkdirs())
                        throw new IOException();
                }

                InputStream is = zf.getInputStream(item);
                FileOutputStream fos = new FileOutputStream(newfilePath);
                byte[] buffer = new byte[512];
                int readchars = 0;
                while( (readchars = is.read(buffer)) != -1 ) {
                    fos.write(buffer, 0, readchars);
                }
                is.close();
                fos.close();
            }
        }
        zf.close();
    }

    static private void addToZip( String path, String srcFile, ZipOutputStream zip )
            throws IOException {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip, true);
        } else {
            byte[] buf = new byte[1024];
            int len;
            FileInputStream in = null;
            try {
                in = new FileInputStream(srcFile);
                zip.putNextEntry(new ZipEntry(path + File.separator + folder.getName()));
                while( (len = in.read(buf)) > 0 ) {
                    zip.write(buf, 0, len);
                }
            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    static private void addFolderToZip( String path, String srcFolder, ZipOutputStream zip,
            boolean addFolder ) throws IOException {
        File folder = new File(srcFolder);
        String listOfFiles[] = folder.list();
        for( int i = 0; i < listOfFiles.length; i++ ) {
            String folderPath = null;
            if (path.length() < 1) {
                folderPath = folder.getName();
            } else {
                folderPath = path + File.separator + folder.getName();
            }
            String srcFile = srcFolder + File.separator + listOfFiles[i];
            addToZip(folderPath, srcFile, zip);
        }
    }

    // public static void main( String[] args ) throws IOException {
    // String zipPath = "C:\\Users\\moovida\\Desktop\\plugins\\geonotes_2.zip";
    // File zipFile = new File(zipPath);
    // File rootFolder = zipFile.getParentFile();
    //
    // unzipFolder(zipPath, rootFolder.getAbsolutePath());
    //
    // }

}