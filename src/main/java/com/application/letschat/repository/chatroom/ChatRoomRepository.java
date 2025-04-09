package com.application.letschat.repository.chatroom;

import com.application.letschat.entity.chatroom.ChatRoom;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<Integer> findUserIdsByChatRoomId(Long chatRoomId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatRoom c SET c.chatRoomName = :chatRoomName, c.updatedAt = :updatedAt WHERE c.chatRoomId = :chatRoomId")
    int updateChatRoomName(@Param("chatRoomId") Long chatRoomId,
                           @Param("chatRoomName") String chatRoomName,
                           @Param("updatedAt") Timestamp updatedAt);
}
