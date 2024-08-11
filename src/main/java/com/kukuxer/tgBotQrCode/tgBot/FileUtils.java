package com.kukuxer.tgBotQrCode.tgBot;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.*;

@Component
public class FileUtils {
    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    // Method to convert File to byte array
    public static byte[] fileToByteArray(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return inputStreamToByteArray(fileInputStream);
        }
    }
    public static byte[] getByteArrayFromInputFile(InputFile inputFile) throws IOException {
        if (inputFile.isNew()) {
            if (inputFile.getNewMediaFile() != null) {
                // Handle file case
                return fileToByteArray(inputFile.getNewMediaFile());
            } else if (inputFile.getNewMediaStream() != null) {
                // Handle InputStream case
                return inputStreamToByteArray(inputFile.getNewMediaStream());
            } else {
                throw new IOException("InputFile does not contain valid media.");
            }
        } else {
            throw new IOException("InputFile is not new and does not contain media.");
        }
    }

}
