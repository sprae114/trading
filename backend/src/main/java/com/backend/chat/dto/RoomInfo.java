package com.backend.chat.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RoomInfo {
    public String roomId;
    public String createCustomer;
}
