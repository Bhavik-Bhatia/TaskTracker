package com.ab.tasktracker.util;

import com.ab.jwt.JwtUtil;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JwtUtil.class})
@TestPropertySource(locations = "classpath:application.properties")
public class JWTUtilTest {
    @Autowired
    private JwtUtil jwtUtil;
    @MockBean
    private Date date;

    @Test
    public void testGenerateJWTTokenSuccess(){
        String signedEncryptedToken=jwtUtil.generateJWTToken("bhavikbhatia9@gmail.com",101L);
        Assert.assertEquals(jwtUtil.extractAllClaims(signedEncryptedToken).get("userId"),101);
    }

    @Test
    public void testGenerateJWTTokenEmailNullAndUserIdNull(){
        String signedEncryptedToken=jwtUtil.generateJWTToken(null,null);
        Assert.assertNull(jwtUtil.extractAllClaims(signedEncryptedToken).getSubject());
    }

    @Test
    public void testGenerateJWTTokenUserIdNull(){
        String signedEncryptedToken=jwtUtil.generateJWTToken("bb@gmail.com",null);
        Assert.assertNull(jwtUtil.extractAllClaims(signedEncryptedToken).get("userId"));
    }

    @Test
    public void testExtractAllClaimsSuccess(){
        String signedEncryptedToken=jwtUtil.generateJWTToken("bhavikbhatia9@gmail.com",101L);
        String username=jwtUtil.extractAllClaims(signedEncryptedToken).getSubject();
        Assert.assertEquals("bhavikbhatia9@gmail.com",username);
    }
    @Test
    public void testExtractAllClaimsEmailNullAndUserIdNull(){
        String signedEncryptedToken=jwtUtil.generateJWTToken(null,null);
        String username=jwtUtil.extractAllClaims(signedEncryptedToken).getSubject();
        Assert.assertEquals(null,username);
    }
    @Test(expected = MalformedJwtException.class)
    public void testExtractAllClaimsInvalidToken(){
        String signedEncryptedToken="3fdf78977#8897";
        jwtUtil.extractUsername(signedEncryptedToken);
    }

    @Test
    public void testIsTokenExpiredFalse(){
        String signedEncryptedToken=jwtUtil.generateJWTToken("bhavikbhatia9@gmail.com",101L);
        boolean result=jwtUtil.isTokenExpired(signedEncryptedToken);
        Assert.assertFalse(result);
    }

    @Test
    public void testIsTokenExpiredTrue(){
        String signedEncryptedToken="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJiaGF2aWtiaGF0aWE5QGdtYWlsLmNvbSIsImlhdCI6MTcwODU0NTE1NiwiZXhwIjoxNzA4NDU4NzU2fQ.p2VfqYKgcQgi7bO3wUgAXGv2-mZkWMH7rETb7jpZMfNZbKNoLH2HxoZFj6Cewz0vdHUxIBPJ5Z_F2Ze0hy_-Og";
        boolean result = jwtUtil.isTokenExpired(signedEncryptedToken);
        Assert.assertTrue(result);
    }

}
