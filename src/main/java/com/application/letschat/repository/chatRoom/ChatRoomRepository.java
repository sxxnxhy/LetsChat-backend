package com.application.letschat.repository.chatRoom;

import com.application.letschat.model.chatRoom.ChatRoom;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {


    List<Integer> findUserIdsByChatRoomId(Long chatRoomId);

    @Modifying
    @Transactional
    @Query("UPDATE ChatRoom c SET c.chatRoomName = :chatRoomName WHERE c.chatRoomId = :chatRoomId")
    int updateChatRoomName(@Param("chatRoomId") Long chatRoomId, @Param("chatRoomName") String chatRoomName);
}
