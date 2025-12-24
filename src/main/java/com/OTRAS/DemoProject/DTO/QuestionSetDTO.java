package com.OTRAS.DemoProject.DTO;
 
import lombok.*;

import java.util.List;
 
@Getter

@Setter

@NoArgsConstructor

@AllArgsConstructor

@Builder

public class QuestionSetDTO {

    private Long id;

    private String setName;

   // private List<QuestionDTO> questions;
    
    private List<QuestionCategoryDTO> categories;

}

 