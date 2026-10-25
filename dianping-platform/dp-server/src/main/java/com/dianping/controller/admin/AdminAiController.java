package com.dianping.controller.admin;

import com.dianping.common.result.Result;
import com.dianping.dto.AiQueryDTO;
import com.dianping.service.Text2SqlService;
import com.dianping.vo.Text2SqlResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 AI 数据助手（自然语言 → SQL → 只读执行）
 */
@RestController
@RequestMapping("/api/admin/ai")
public class AdminAiController {

    @Autowired
    private Text2SqlService text2SqlService;

    @PostMapping("/text2sql")
    public Result<Text2SqlResult> text2sql(@RequestBody @Valid AiQueryDTO dto) {
        return Result.success(text2SqlService.ask(dto.getQuestion()));
    }
}