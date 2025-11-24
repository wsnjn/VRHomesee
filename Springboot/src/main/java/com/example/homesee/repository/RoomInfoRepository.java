package com.example.homesee.repository;

import com.example.homesee.entity.RoomInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomInfoRepository extends JpaRepository<RoomInfo, Long>, JpaSpecificationExecutor<RoomInfo> {

    // 根据状态查询并按照创建时间倒序排列
    List<RoomInfo> findByStatusOrderByCreatedTimeDesc(Integer status);

    // 获取不重复的省份列表
    @Query("SELECT DISTINCT r.province FROM RoomInfo r WHERE r.province IS NOT NULL AND r.province != '' ORDER BY r.province")
    List<String> findDistinctProvinces();

    // 根据省份获取不重复的城市列表
    @Query("SELECT DISTINCT r.city FROM RoomInfo r WHERE r.province = :province AND r.city IS NOT NULL AND r.city != '' ORDER BY r.city")
    List<String> findDistinctCitiesByProvince(@Param("province") String province);

    // 根据省份和城市获取不重复的区县列表
    @Query("SELECT DISTINCT r.district FROM RoomInfo r WHERE r.province = :province AND r.city = :city AND r.district IS NOT NULL AND r.district != '' ORDER BY r.district")
    List<String> findDistinctDistrictsByProvinceAndCity(@Param("province") String province, @Param("city") String city);

    // 根据省份、城市和区县获取不重复的街道列表
    @Query("SELECT DISTINCT r.street FROM RoomInfo r WHERE r.province = :province AND r.city = :city AND r.district = :district AND r.street IS NOT NULL AND r.street != '' ORDER BY r.street")
    List<String> findDistinctStreetsByProvinceAndCityAndDistrict(@Param("province") String province,
            @Param("city") String city, @Param("district") String district);

    // 根据省份、城市、区县和街道获取不重复的小区列表
    @Query("SELECT DISTINCT r.communityName FROM RoomInfo r WHERE r.province = :province AND r.city = :city AND r.district = :district AND r.street = :street AND r.communityName IS NOT NULL AND r.communityName != '' ORDER BY r.communityName")
    List<String> findDistinctCommunitiesByProvinceAndCityAndDistrictAndStreet(@Param("province") String province,
            @Param("city") String city, @Param("district") String district, @Param("street") String street);

    // 根据省份、城市和小区名称获取房源
    List<RoomInfo> findByProvinceAndCityAndCommunityNameAndStatusOrderByCreatedTimeDesc(String province, String city,
            String communityName, Integer status);

    // 根据省份、城市、区县和状态获取房源
    List<RoomInfo> findByProvinceAndCityAndDistrictAndStatusOrderByCreatedTimeDesc(String province, String city,
            String district, Integer status);

    // 根据租赁类型和状态获取房源
    List<RoomInfo> findByRentalTypeAndStatusOrderByCreatedTimeDesc(Integer rentalType, Integer status);

    // 根据装修程度和状态获取房源
    List<RoomInfo> findByDecorationAndStatusOrderByCreatedTimeDesc(Integer decoration, Integer status);

    // 根据价格范围获取房源
    @Query("SELECT r FROM RoomInfo r WHERE r.rentPrice BETWEEN :minPrice AND :maxPrice AND r.status = :status ORDER BY r.createdTime DESC")
    List<RoomInfo> findByRentPriceBetweenAndStatusOrderByCreatedTimeDesc(@Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice, @Param("status") Integer status);

    // 根据面积范围获取房源
    @Query("SELECT r FROM RoomInfo r WHERE r.roomArea BETWEEN :minArea AND :maxArea AND r.status = :status ORDER BY r.createdTime DESC")
    List<RoomInfo> findByRoomAreaBetweenAndStatusOrderByCreatedTimeDesc(@Param("minArea") Double minArea,
            @Param("maxArea") Double maxArea, @Param("status") Integer status);

    // 获取所有区县（不限制省份和城市）
    @Query("SELECT DISTINCT r.district FROM RoomInfo r WHERE r.district IS NOT NULL AND r.district != '' ORDER BY r.district")
    List<String> findDistinctDistricts();

    // 根据区县获取街道（不限制省份和城市）
    @Query("SELECT DISTINCT r.street FROM RoomInfo r WHERE r.district = :district AND r.street IS NOT NULL AND r.street != '' ORDER BY r.street")
    List<String> findDistinctStreetsByDistrict(@Param("district") String district);

    // 根据城市获取不重复的区县列表（不限制省份）
    @Query("SELECT DISTINCT r.district FROM RoomInfo r WHERE r.city = :city AND r.district IS NOT NULL AND r.district != '' ORDER BY r.district")
    List<String> findDistinctDistrictsByCity(@Param("city") String city);

    // 根据街道获取小区（不限制省份、城市和区县）
    @Query("SELECT DISTINCT r.communityName FROM RoomInfo r WHERE r.street = :street AND r.communityName IS NOT NULL AND r.communityName != '' ORDER BY r.communityName")
    List<String> findDistinctCommunitiesByStreet(@Param("street") String street);

    // 根据状态查询房屋
    List<RoomInfo> findByStatus(Integer status);

    // 根据状态统计房屋数量
    long countByStatus(Integer status);

    // 根据房东手机号查询房屋列表
    List<RoomInfo> findByLandlordPhoneOrderByCreatedTimeDesc(String landlordPhone);

    // 根据房东手机号和状态查询房屋列表
    List<RoomInfo> findByLandlordPhoneAndStatusOrderByCreatedTimeDesc(String landlordPhone, Integer status);

    // 根据房东手机号统计房屋数量
    long countByLandlordPhone(String landlordPhone);

    // 根据房东手机号和状态统计房屋数量
    // 根据房东手机号和状态统计房屋数量
    long countByLandlordPhoneAndStatus(String landlordPhone, Integer status);

    // 统计各城市的房屋数量
    @Query("SELECT r.city, COUNT(r) FROM RoomInfo r WHERE r.city IS NOT NULL AND r.city != '' GROUP BY r.city")
    List<Object[]> countByCity();
}
