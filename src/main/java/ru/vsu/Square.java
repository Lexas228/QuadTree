package ru.vsu;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Square {
    private Point point;
    private int width;
    private int height;
}
