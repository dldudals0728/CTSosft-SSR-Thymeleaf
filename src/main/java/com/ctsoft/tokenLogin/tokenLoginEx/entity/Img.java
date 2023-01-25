package com.ctsoft.tokenLogin.tokenLoginEx.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table
@Getter
@Setter
@ToString
public class Img {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private long boardIdx;

    @Column(nullable = false)
    private String filepath;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String originFilename;
}
