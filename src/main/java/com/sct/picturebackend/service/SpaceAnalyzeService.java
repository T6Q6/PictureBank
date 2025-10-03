package com.sct.picturebackend.service;

import com.sct.picturebackend.model.dto.analyze.*;
import com.sct.picturebackend.model.entity.Space;
import com.sct.picturebackend.model.entity.User;
import com.sct.picturebackend.model.vo.space.analyze.*;

import java.util.List;

public interface SpaceAnalyzeService {
    SpaceUsageAnalyzeResponse getUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    List<SpaceUserAnalyzeResponse> getSpaceUserUploadActionAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    List<Space> getSpaceRank(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
