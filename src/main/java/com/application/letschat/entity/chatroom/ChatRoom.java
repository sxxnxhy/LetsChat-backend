package com.application.letschat.entity.chatroom;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_room_id")
    private Long chatRoomId;

    @Column(name="chat_room_name", length = 255)
    private String chatRoomName;

    @Column(name="enrolled_at")
    private Timestamp enrolledAt;

    @Column(name="updated_at")
    private Timestamp updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.enrolledAt == null) {
            this.enrolledAt = new Timestamp(System.currentTimeMillis());
        }
    }

}
