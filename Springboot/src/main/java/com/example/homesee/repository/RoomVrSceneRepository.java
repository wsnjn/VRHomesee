package com.example.homesee.repository;

import com.example.homesee.entity.RoomVrScene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomVrSceneRepository extends JpaRepository<RoomVrScene, Long> {
    List<RoomVrScene> findByRoomId(Long roomId);
}
