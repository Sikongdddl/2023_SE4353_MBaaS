package com.tiger.baas.Service;

import com.tiger.baas.utils.RegisterBuffer;
import org.springframework.stereotype.Service;

@Service
public class SynService {
    private RegisterBuffer registerBuffer = new RegisterBuffer();

    public String subscribe(String databaseId, String tableId, String deviceId){
        registerBuffer.add(databaseId, tableId, deviceId);
        return "0";
    }

    public String unSubscribe(String databaseId, String tableId, String deviceId){
        registerBuffer.delete(databaseId,tableId,deviceId);
        return "0";
    }
}
