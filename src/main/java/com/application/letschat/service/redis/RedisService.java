package com.application.letschat.service.redis;


import com.application.letschat.dto.chatroomuser.ChatRoomUserDTO;
import com.application.letschat.dto.message.MessageDTO;
import com.application.letschat.repository.chatroomuser.ChatRoomUserRepository;
import com.application.letschat.service.notification.NotificationService;
import com.application.letschat.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, MessageDTO> redisTemplate;

    private final RedisTemplate<String, ChatRoomUserDTO> chatRoomUserRedisTemplate;

    private final RedisTemplate<String, Integer> userIdRedisTemplate;

    private final RedisTemplate<String, Long> chatRoomIdRedisTemplate;

    private final UserService userService;

    private final NotificationService notificationService;

    private final ChatRoomUserRepository chatRoomUserRepository;




    public void addPendingMessage(MessageDTO messageDTO) throws Exception {
        String key = "message_queue:" + messageDTO.getChatRoomId();
        redisTemplate.opsForList().rightPush(key, messageDTO); // Add to end

        //toggle refresh to all users of that chatroom that is in the chat list
        notificationService.toggleRefresh(getUserIdsByChatRoomId(messageDTO.getChatRoomId()));

    }

    public List<MessageDTO> getPendingMessages(Long chatRoomId) {
        String key = "message_queue:" + chatRoomId;
        List<MessageDTO> messages = redisTemplate.opsForList().range(key, 0, -1);
        return messages != null ? messages : Collections.emptyList();
    }

    public List<MessageDTO> getAllPendingMessages() {
        try {
            String pattern = "message_queue:*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys.isEmpty()) {
                return Collections.emptyList();
            }
            List<MessageDTO> allMessages = new ArrayList<>();
            for (String key : keys) {
                List<MessageDTO> messages = redisTemplate.opsForList().range(key, 0, -1);
                if (messages != null) {
                    allMessages.addAll(messages);
                }
            }
            return allMessages.isEmpty() ? Collections.emptyList() : allMessages;
        } catch (Exception e) {
            log.error("Failed to fetch all pending messages from Redis", e);
            return Collections.emptyList();
        }
    }

    public void removePendingMessage(Long chatRoomId) {
        String key = "message_queue:" + chatRoomId;
        redisTemplate.delete(key); // Simply delete the entire list for this chat room
        log.info("Removing pending messages of chat room {}", chatRoomId);
    }

    public void removeAllPendingMessages() {
        try {
            String pattern = "message_queue:*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Failed to remove all pending messages from Redis", e);
        }
    }


    public void addPendingLastReadAt(ChatRoomUserDTO chatRoomUserDto) {
        String key = "lastReadTimer:" + chatRoomUserDto.getUserId() + ":" + chatRoomUserDto.getChatRoomId();
        chatRoomUserRedisTemplate.opsForValue().set(key, chatRoomUserDto);
        log.info("마지막 접속 시간: {}, 유저id: {}, 챗id: {}",
                chatRoomUserDto.getLastReadAt(),
                chatRoomUserDto.getUserId(),
                chatRoomUserDto.getChatRoomId());
    }

    public List<ChatRoomUserDTO> getPendingLastReadAt(Integer userId) {
        String pattern = "lastReadTimer:" + userId + ":*";
        List<ChatRoomUserDTO> chatRoomUserDtos = new ArrayList<>();
        try {
            // Get all matching keys
            Set<String> keys = chatRoomUserRedisTemplate.keys(pattern);
                List<ChatRoomUserDTO> dtos = chatRoomUserRedisTemplate.opsForValue().multiGet(keys);
                if (dtos != null) {
                    // Filter out nulls and add to result
                    dtos.stream()
                            .filter(dto -> dto != null)
                            .forEach(chatRoomUserDtos::add);
                }
            return chatRoomUserDtos.isEmpty() ? Collections.emptyList() : chatRoomUserDtos;
        } catch (Exception e) {
            log.error("Failed to fetch pending last read times from Redis for userId: {}", userId, e);
            return Collections.emptyList();
        }
    }
    //해당하는 방의 유저들의 lastReadAt
//    public List<ChatRoomUserDTO> getAllLastReadAtByChatRoomId(Long chatRoomId) {
//        try {
//            // 1. Get all user IDs in the chat room
//            List<Integer> userIds = getUserIdsByChatRoomId(chatRoomId);
//
//            if (userIds.isEmpty()) {
//                log.info("No users found for chatRoomId: {}", chatRoomId);
//                return Collections.emptyList();
//            }
//
//            // 2. Construct specific keys for this chat room
//            List<String> keys = userIds.stream()
//                    .map(userId -> "lastReadTimer:" + userId + ":" + chatRoomId)
//                    .collect(Collectors.toList());
//
//            // 3. Bulk fetch from Redis
//            List<ChatRoomUserDTO> results = chatRoomUserRedisTemplate.opsForValue()
//                    .multiGet(new HashSet<>(keys))
//                    .stream()
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            log.info("Retrieved {} last read times for chatRoomId: {}", results.size(), chatRoomId);
//            return results.isEmpty() ? Collections.emptyList() : results;
//
//        } catch (Exception e) {
//            log.error("Failed to fetch all last read times for chatRoomId: {}", chatRoomId, e);
//            return Collections.emptyList();
//        }
//    }

    public void removePendingLastReadAt(Integer userId) {
        String pattern = "lastReadTimer:" + userId + ":*";
        try {
            // Get all keys matching the pattern
            Set<String> keys = chatRoomUserRedisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                // Delete all matching keys
                chatRoomUserRedisTemplate.delete(keys);
                log.info("Removed {} pending last read entries for userId: {}", keys.size(), userId);
            }
        } catch (Exception e) {
            log.error("Failed to remove pending last read entries from Redis for userId: {}", userId, e);
        }
    }


    public List<Integer> getUserIdsByChatRoomId(Long chatRoomId) {
        String key = "chatroom_users:" + chatRoomId;
        Set<Integer> userIds = userIdRedisTemplate.opsForSet().members(key);

        if (userIds == null || userIds.isEmpty()) {
            try {
                List<Integer> dbUserIds = chatRoomUserRepository.findUserIdsByChatRoomId(chatRoomId);
                if (!dbUserIds.isEmpty()) {
                    userIdRedisTemplate.opsForSet().add(key, dbUserIds.toArray(new Integer[0]));
                    userIdRedisTemplate.expire(key, 24 * 60 * 60, TimeUnit.SECONDS); // 24 hours TTL
                    log.info("Cached {} user IDs for chat room {} in Redis with 24-hour expiry", dbUserIds.size(), chatRoomId);
                    return dbUserIds;
                }
                return Collections.emptyList();
            } catch (Exception e) {
                log.error("Failed to fetch user IDs from DB for chatRoomId: {}", chatRoomId, e);
                return Collections.emptyList();
            }
        }
        return new ArrayList<>(userIds);
    }



    public List<Long> getChatRoomIdsByUserId(Integer userId) {
        String key = "user_chatrooms:" + userId;
        Set<Long> chatRoomIds = chatRoomIdRedisTemplate.opsForSet().members(key);

        //레디스에 없으면 디비에서 불러오기.
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            try {
                List<Long> dbChatRoomIds = chatRoomUserRepository.findChatRoomIdsByUserId(userId);
                if (!dbChatRoomIds.isEmpty()) {
                    chatRoomIdRedisTemplate.opsForSet().add(key, dbChatRoomIds.toArray(new Long[0]));
                    chatRoomIdRedisTemplate.expire(key, 24 * 60 * 60, TimeUnit.SECONDS); // 24 hours TTL
                    log.info("Cached {} chat room IDs for user {} in Redis with 24-hour expiry", dbChatRoomIds.size(), userId);
                    return dbChatRoomIds;
                }
                return Collections.emptyList();
            } catch (Exception e) {
                log.error("Failed to fetch chat room IDs from DB for userId: {}", userId, e);
                return Collections.emptyList();
            }
        }
        return new ArrayList<>(chatRoomIds);
    }


    //두가지로 나눈 이유는 유저의 입장, 채팅방의 입장. 따로 저장해야 찾기가 쉬워짐. 쿼리가 그나마 덜 복잡해짐 -> 성능 개선 예상.
    public void addChatRoomIdsAndUserIds(Integer userId, Long chatRoomId) {
        String keyForUserId = "user_chatrooms:" + userId;
        Set<Long> chatRoomIds = chatRoomIdRedisTemplate.opsForSet().members(keyForUserId);
        //레디스에 없으면 디비에서 불러오기.(expired 되어서 없어짐)
        if (chatRoomIds == null || chatRoomIds.isEmpty()) {
            List<Long> dbChatRoomIds = chatRoomUserRepository.findChatRoomIdsByUserId(userId);
            chatRoomIdRedisTemplate.opsForSet().add(keyForUserId, dbChatRoomIds.toArray(new Long[0]));
            chatRoomIdRedisTemplate.expire(keyForUserId, 24 * 60 * 60, TimeUnit.SECONDS); // 24 hours TTL
            log.info("Cached {} chat room IDs for user {} in Redis with 24-hour expiry", dbChatRoomIds.size(), userId);
        }
        // Add chatRoomId to user's set
        chatRoomIdRedisTemplate.opsForSet().add(keyForUserId, chatRoomId);
        chatRoomIdRedisTemplate.expire(keyForUserId, 24 * 60 * 60, TimeUnit.SECONDS); // 24 hours TTL
        log.info("Added chatRoomId {} to user {} in Redis with 24-hour expiry", chatRoomId, userId);


        String keyForChatRoomId = "chatroom_users:" + chatRoomId;
        Set<Integer> userIds = userIdRedisTemplate.opsForSet().members(keyForChatRoomId);
        //레디스에 없으면 디비에서 불러오기.(expired 되어서 없어짐)
        if (userIds == null || userIds.isEmpty()) {
            List<Integer> dbUserIds = chatRoomUserRepository.findUserIdsByChatRoomId(chatRoomId);
            userIdRedisTemplate.opsForSet().add(keyForChatRoomId, dbUserIds.toArray(new Integer[0]));
            userIdRedisTemplate.expire(keyForChatRoomId, 24 * 60 * 60, TimeUnit.SECONDS); // 24 hours TTL
            log.info("Cached {} user IDs for chat room {} in Redis with 24-hour expiry", dbUserIds.size(), chatRoomId);
        }

        // Add userId to chat room's set
        userIdRedisTemplate.opsForSet().add(keyForChatRoomId, userId);
        userIdRedisTemplate.expire(keyForChatRoomId, 24 * 60 * 60, TimeUnit.SECONDS); // 24 hours TTL
        log.info("Added userId {} to chatRoomId {} in Redis with 24-hour expiry", userId, chatRoomId);
    }

    public void removeChatRoomIdsAndUserIds(Integer userId, Long chatRoomId) {
        String keyForUserId = "user_chatrooms:" + userId;
        String keyForChatRoomId = "chatroom_users:" + chatRoomId;

        // Remove chatRoomId from the user's chat rooms set
        chatRoomIdRedisTemplate.opsForSet().remove(keyForUserId, chatRoomId);
        log.info("Removed chatRoomId {} from user {} in Redis", chatRoomId, userId);

        // Remove userId from the chat room's users set
        userIdRedisTemplate.opsForSet().remove(keyForChatRoomId, userId);
        log.info("Removed userId {} from chatRoomId {} in Redis", userId, chatRoomId);

    }


    public void addEmailVerificationCode(String email, int generatedVerificationCode) {
        String keyForEmailVerificationCode = "email_verification_code:" + email;
        //5분동안 redis에 저장
        userIdRedisTemplate.opsForValue().set(keyForEmailVerificationCode, generatedVerificationCode, 5, TimeUnit.MINUTES);
    }

    public Object getEmailVerificationCode(String email) {
        String keyForEmailVerificationCode = "email_verification_code:" + email;
        return userIdRedisTemplate.opsForValue().get(keyForEmailVerificationCode);
    }


}
