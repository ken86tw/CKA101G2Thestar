package com.thestar.refund.service;

import com.thestar.refund.dto.RefundDTO;
import com.thestar.refund.entity.RefundListVO;
import com.thestar.refund.repository.RefundListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RefundService {


    private final RefundListRepository refundListRepository;

    public RefundService(RefundListRepository refundListRepository) {

        this.refundListRepository = refundListRepository;
    }

    @Transactional(readOnly = true)
    public List<RefundDTO> findPendingRefunds() {
        List<RefundDTO> dtoList = new ArrayList<>();

        List<RefundListVO> list = refundListRepository.findByRefundStatusOrderByRefundId((byte) 0);
        for (RefundListVO item : list) {
            RefundDTO dto = new RefundDTO();
            dto.setOrderId(item.getOrdervo().getOrderId());
            dto.setMemberId(item.getOrdervo().getMemberId());
            dto.setRefundId(item.getRefundId());
            dto.setAmount(item.getAmount());
            dto.setReason(item.getReason());
            dto.setCreatedTime(item.getCreatedTime());
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Transactional
    public void processRefund(Integer employeeId, Integer refundId) {

        int row = refundListRepository.refundOrder(employeeId, refundId);
        if (row == 0) {
            throw new IllegalStateException("此訂單已處理或是不存在");
        }
    }
}

