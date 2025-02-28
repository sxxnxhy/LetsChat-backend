package com.application.letschat.repository.chatRoom;

import com.application.letschat.model.chatRoom.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {


    List<Integer> findUserIdsByChatRoomId(Long chatRoomId);

}
