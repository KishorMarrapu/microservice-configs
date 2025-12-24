package com.OTRAS.DemoProject.DTO;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionCategoryDTO {

    private Long id;

    private String questionCategoryName; // GK, Maths, Reasoning

    private List<QuestionDTO> questions;
}
