package com.wangn.gendir;

import java.lang.Integer;
import java.lang.String;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(
    name = "Demo"
)
@Entity
public class Demo {
  @Column(
      name = "errcode"
  )
  private Integer errcode;

  @Column(
      name = "errmsg"
  )
  private String errmsg;

  @Column(
      name = "access_token"
  )
  private String accessToken;

  @Column(
      name = "expires_in"
  )
  private Integer expiresIn;

  @Column(
      name = "permanent_code"
  )
  private String permanentCode;

  @Column(
      name = "listTest"
  )
  private List<Integer> listtest;

  @Column(
      name = "auth_corp_info"
  )
  private List<AuthCorpInfo> authCorpInfo;
}
