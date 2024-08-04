package com.kukuxer.tgBotQrCode.qrCodeVisitor;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Service
public class GeolocationService {

    private final DatabaseReader dbReader;

    public GeolocationService(@Value("${geoip.database.path}") String databasePath) throws IOException {
        File database = new File(databasePath);
        if (!database.exists()) {
            throw new IOException("GeoLite2-Country.mmdb file not found at " + databasePath);
        }
        this.dbReader = new DatabaseReader.Builder(database).build();
    }

    public String getCountryByIp(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CountryResponse response = dbReader.country(ipAddress);
            return response.getCountry().getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    public String getCityByIp(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = dbReader.city(ipAddress);
            return response.getCity().getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}
