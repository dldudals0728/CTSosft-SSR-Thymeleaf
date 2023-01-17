package com.ctsoft.tokenLogin.tokenLoginEx.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@Setter
@ToString
public class Board {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false)
    private LocalDateTime regTime;

    @Column(nullable = false)
    private long count;

    @Column(nullable = false)
    @Lob
    private String content;

    private String filename;

    private String filepath;
}
