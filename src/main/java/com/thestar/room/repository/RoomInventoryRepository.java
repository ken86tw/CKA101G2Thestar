package com.example.thestar1.room.repository;

import com.example.thestar1.room.entity.RoomInventoryId;
import com.example.thestar1.room.entity.RoomInventoryVO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;


// 泛型一 = 這個倉庫管哪個 entity（RoomInventoryVO）
// 泛型二 = 那個 entity 的主鍵型別（複合主鍵 RoomInventoryId）
public interface RoomInventoryRepository extends JpaRepository<RoomInventoryVO, RoomInventoryId> {

    //createOrder用來初始化庫存
    @Modifying
    @Query(value = "INSERT INTO ROOM_INVENTORY(INVENTORY_DATE,ROOM_TYPE_ID, TOTAL_AMOUNT,BOOKED_AMOUNT) " +
            "SELECT :date,:roomTypeId,rt.ROOM_TYPE_AMOUNT,0 FROM ROOM_TYPE rt WHERE  rt.ROOM_TYPE_ID = :roomTypeId " +
            "ON DUPLICATE KEY UPDATE BOOKED_AMOUNT = BOOKED_AMOUNT", nativeQuery = true)
    void initInventory(@Param("date") LocalDate date,
                       @Param("roomTypeId") Integer roomTypeId);

    //                        由於是動態建立需要先初始化表格如果db裡已經有這天這個房型就不做更新直接進入訂房流程
    //                        如果沒有將日期還有房型以及房型的總數以及未下單所以已訂房數還是零的資料送進資料庫(房型總數需要從房型表取得)



    //createOrder用來訂房
    @Modifying
    @Query(value = "UPDATE ROOM_INVENTORY SET BOOKED_AMOUNT = BOOKED_AMOUNT + :qty WHERE INVENTORY_DATE = :date " +
            "AND ROOM_TYPE_ID = :roomTypeId AND BOOKED_AMOUNT + :qty <= TOTAL_AMOUNT", nativeQuery = true)
        // 把已訂數加上要訂的數量, (鎖定哪一天 , 鎖定哪個房型) 這兩行合起來＝主鍵 int = 1 扣成功；0＝塞不下,訂不到
    int bookRooms(@Param("date") LocalDate date,
                  @Param("roomTypeId") Integer roomTypeId,
                  @Param("qty") int qty);

    //                         動態建立後訂房，將要新增的房數新增至列表中並且
    //                         在日期與房型一樣的前提下預定的房數加上已存在的被訂房數必須小於等於總房數
    //                         才能完成預訂否則直接回傳0成功回傳1



    //cancelOrder跟cleanExpiredOrder用來釋放庫存
    @Modifying
    @Query(value = "UPDATE ROOM_INVENTORY SET BOOKED_AMOUNT = BOOKED_AMOUNT - :qty " +
            "WHERE INVENTORY_DATE = :date AND ROOM_TYPE_ID = :roomTypeId", nativeQuery = true)
    int releaseRoom(@Param("date") LocalDate date,
                    @Param("roomTypeId") Integer roomTypeId,
                    @Param("qty") int qty);



    //redis用來查詢目前庫存房間數有多少
    @Query(value = "SELECT TOTAL_AMOUNT-BOOKED_AMOUNT FROM ROOM_INVENTORY " +
            "WHERE INVENTORY_DATE = :date AND ROOM_TYPE_ID = :roomTypeId", nativeQuery = true)
    Integer checkInventory(@Param("roomTypeId") Integer roomTypeId,
                           @Param("date") LocalDate date);
}

