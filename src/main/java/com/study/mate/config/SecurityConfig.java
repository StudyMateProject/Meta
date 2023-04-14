package com.study.mate.config;

import com.study.mate.service.SignUpOAuthService;
import com.study.mate.service.SignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

@Configuration
@EnableWebSecurity // 웹보안 활성화를 위한 어노테이션
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // @Autowired
    // UserDetailsService userDetailsService; // 시스템에 있는 사용자 계정을 조회할때 사용할 서비스

    @Autowired
    SignUpService signUpService; // UserDetailsService를 implements한 서비스 (시스템 --> DB)
                                 // 로그인에서 DB에 있는 사용자 계정을 조회할때 사용할 서비스
                                 // Remember me에서 DB에 있는 사용자 계정을 조회할때 사용할 서비스

    @Autowired
    SignUpOAuthService signUpOAuthService; // OAuth2UserService를 implements한 서비스
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable(); // Spring Security에서 POST방식을 사용하기 위한 메소드
                               // Spring Security 설정들을 활성화시켜 준다.

        // 권한 설정
        http
                //.antMatcher("/") // 특정 경로를 지정, 해당 메소드를 생략하면 모든 경로에 대해 검색하게 된다.
                .authorizeRequests() // 보안 검사기능 시작
                .antMatchers("/", "/n", "/joinform/**", "/loginform/**").permitAll() // 해당경로에 대한 모든 접근을 허용한다.
                .antMatchers("/ws/**", "/meta/**", "/mypage/**").hasRole("USER") // 로그인은 USER권한을 가지고 있는 사용자에게만 허용한다.
                .antMatchers("/mypage/pay").access("hasRole('ADMIN')")
                .anyRequest().authenticated(); // 나머지 요청들은 권한의 종류에 상관 없이 권한이 있어야 접근 가능하다.

        http
                .exceptionHandling()
                        .accessDeniedPage("/"); // 권한이 없는 대상이 접속을 시도했을 때 이동할 경로

        // 로그인
        http
                .formLogin() // 보안 검증은 formLogin방식으로 한다.
                .loginPage("/loginform") // 사용자 정의 로그인 페이지
                .defaultSuccessUrl("/") // 로그인 성공 후 이동 페이지 - 핸들러를 사용할 경우 필요없다.
                .failureUrl("/loginform?error=true") // 로그인 실패 후 이동 페이지 - 핸들러를 사용할 경우 필요없다.
                .usernameParameter("emailId") // 아이디 파라미터명 설정 - 로그인 form에 아이디 name명과 동일하게 작성한다.
                .passwordParameter("pwd") // 비밀번호 파라미터명 설정 - 로그인 form에 비밀번호 name명과 동일하게 작성한다.
                .loginProcessingUrl("/loginform/login") // 로그인 Form Action Url
                .successHandler(new AuthenticationSuccessHandler() { // 로그인 성공 후 핸들러 (해당 핸들러를 생성하여 핸들링 해준다.)
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        Authentication authentication) throws IOException, ServletException {
                        System.out.println("authentication : " + authentication.getName());

                        response.sendRedirect("/"); // 로그인 성공 후 이동 페이지
                    }
                })
                .failureHandler(new AuthenticationFailureHandler() { // 로그인 실패 후 핸들러 (해당 핸들러를 생성하여 핸들링 해준다.)
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        AuthenticationException exception) throws IOException, ServletException {
                        System.out.println("exception : " + exception.getMessage());

                        // 에러 메세지를 작성할 변수
                        String errorMsg = null;

                        // 아이디 및 비밀번호가 틀릴 경우
                        if ( exception instanceof BadCredentialsException ) {
                            errorMsg = "아이디 또는 비밀번호가 잘못 입력 되었습니다.";
                        // 그 외 모든 에러일 경우
                        } else {
                            errorMsg = "알수없는 이유로 로그인에 실패하였습니다.";
                        }

                        // 로그인 실패 후 이동 페이지
                        // URLEncoder.encode() - URL을 통해 전송하는 데이터에 공백이 있거나 특수문자 및 한글 등으로 되어 있는 경우 URL에 맞게 인코딩 해주는 메소드이다. (공백은 +로, 한글은 16진수로 인코딩 한다.)
                        //                       이를 통해 URL에서 사용하는 특수문자나 한글 등을 인코딩하여 안전하게 전송할 수 있다.
                        response.sendRedirect("/loginform?error=true&errorMsg=" + URLEncoder.encode(errorMsg, "UTF-8"));
                        // request.getRequestDispatcher("/loginform").forward(request, response); // 로그인 실패 후 이동 페이지
                    }
                })
                .permitAll(); // 사용자 정의 로그인 페이지 접근 권한 승인

        // OAuth 2.0
        http
                .oauth2Login() // OAuth2 로그인 기능에 대한 여러 설정의 진입점이다.
                .userInfoEndpoint() // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때의 설정들을 담당한다.
                .userService(signUpOAuthService); // 소셜 로그인 성공 후속 조치를 진행할 UserService 인터페이스의 구현체를 등록한다.

        // Remember Me 인증 (자동 로그인)
        http
                .rememberMe() // rememberMe 기능 작동
                .key("autoLoginMember") // 인증받은 사용자의 정보로 token을 생성하는데 사용되는 key값을 설정한다. (임의 값 설정)
                .rememberMeParameter("remember-me") // HTML에서 name에 해당하는 값 - default : "remember-me"
                                                    // 보통 checkbox로 만들텐데, 이때 name명을 여기서 설정한 값과 동일하게 해줘야 한다.
                .tokenValiditySeconds(86400 * 30) // 쿠키의 만료시간 설정(초), default : 14일 (현재 30일로 설정)
                .alwaysRemember(false) // remember me 기능이 활성화되지 않아도 항상 실행 - defalut : false
                .userDetailsService(signUpService) // Remember me에서 DB에 있는 사용자 계정을 조회할때 사용할 서비스
                .authenticationSuccessHandler(new AuthenticationSuccessHandler() { // remember-me로 로그인 성공 후 핸들러 (해당 핸들러를 생성하여 핸들링 해준다.)
                @Override
                    public void onAuthenticationSuccess(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        Authentication authentication) throws IOException, ServletException {
                        response.sendRedirect("/"); // remember-me로 로그인 성공 후 이동 페이지
                    }
                });

        // 동시 세션 제어 설정
        http
                .sessionManagement() // 세션 관리 기능 작동
                .invalidSessionUrl("/") // 세션이 유효하지 않을 때 이동 할 페이지
                .maximumSessions(1) // 최대 허용 가능 세션 수, (-1 : 무제한)
                .maxSessionsPreventsLogin(true) // 동시 로그인 차단함, false : 기존 세션 만료(defalut)
                .expiredUrl("/"); // 세션이 만료된 경우 이동할 페이지

        // 세션 고정 보호 설정
        http
                .sessionManagement() // 세션 관리 기능 작동
                .sessionFixation().changeSessionId(); // changeSessionId(기본값) -> 세션은 유지하되 세션아이디는 계속 새로 발급
                                                      // none : 세션이 새로 생성되지 않고 그대로 유지되기 때문에 세션 고정 공격에 취약하다.
                                                      // migrateSession : 새로운 세션도 생성되고 세션아이디도 발급된다. + 이전 세션의 속성값들도 유지된다.
                                                      // newSession : 세션이 새롭게 생성되고, 세션아이디도 발급되지만, 이전 세션의 속성값들을 유지할 수 없다.

        // 세션 정책 설정
        http
                .sessionManagement() // 세션 관리 기능 작동
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED); // SessionCreationPolicy.IF_REQUIRED : 스프링 시큐리티가 필요 시 생성(default)
                                                                           // SessionCreationPolicy.Always : 스프링 시큐리티가 항상 세션 생성
                                                                           // SessionCreationPolicy.Never : 스프링 시큐리티가 생성하지 않지만 이미 존재하면 사용
                                                                           // SessionCreationPolicy.Stateless : 스프링 시큐리티가 생성하지 않고 존재해도 사용하지 않는다.
                                                                           //                                   --> JWT 토큰방식을 사용할 때는 Stateless 정책을 사용한다.

        // 로그아웃
        http
                .logout() // 로그아웃 처리
                .logoutUrl("/logout") // 로그아웃 처리 URL
                .logoutSuccessUrl("/") // 로그아웃 성공 후 이동 페이지
                .deleteCookies("JSESSIONID", "remember-me") // 로그아웃 후 쿠키 삭제
                .addLogoutHandler(new LogoutHandler() { // 로그아웃 핸들러
                    @Override
                    public void logout(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) {
                        HttpSession session = request.getSession(); // 현재 들고있는 세션을 가져온다.
                        session.invalidate(); // 가져온 세션을 무효화한다.(제거X)
                                              // 세션을 완전히 제거하기 위해선 아래에 HttpSessionEventPublisher 클래스를 리스너로 등록하여 사용해야한다.
                    }
                })
                .logoutSuccessHandler(new LogoutSuccessHandler() { // 로그아웃 성공 후 핸들러
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                        response.sendRedirect("/"); // 로그아웃 성공 후 이동 페이지
                    }
                });
                //.deleteCookies("remember-me"); // 쿠키 삭제
    } // configure

    // 세션 제거 방식 - 로그아웃시 세션이 제거되지 않는 문제 해결
    // 1. 세션이 생성되거나 페기될 때 호출되는 HttpSessionEventPublisher를 리스너로 등록 - 세션 제거 준비
    // 2. 로그아웃시 HttpSessionEventPublisher의 sessionDestroyed(HttpSessionEvent event) 메소드가 호출되고
    //    ApplicationContext.publishEvent(HttpSessionDestroyedEvent)를 실행하여 HttpSessionDestroyedEvent를 발생 - 세션 제거 시작
    // 3. HttpSessionDestroyedEvent가 실행되면 onApplicationEvent(SessionDestroyedEvent event)가 호출되고
    //    여기에서 removeSessionInformation(sessionId) 구문을 실행하는데 이 구문이 Set 에 저장된 SessionInformation를 삭제 - 세션 제거 완료
    @Bean
    public static ServletListenerRegistrationBean httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                // static 디렉터리의 하위 파일 목록은 인증 무시 (= 항상통과)
                .ignoring().antMatchers("/favicon.ico", "/resources/**", "/error", "/css/**", "/js/**", "/img/**", "/lib/**", "/imagePath/**");
    }

    // 비밀번호 암호화 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder - BCrypt 해싱 함수(BCrypt hashing function)를 사용해서 비밀번호를 인코딩해주는 메소드와
        //                         로그인 진행중인 유저에 의해 제출된 비밀번호와 저장소에 저장되어 있는 비밀번호의 일치 여부를 확인해주는 메소드를 제공한다.
        //                         PasswordEncoder 인터페이스를 구현한 클래스이다.
        return new BCryptPasswordEncoder();
    }

    // 인증 방식 설정
    // Spring Security에서 모든 인증은 AuthenticationManager를 통해 이루어지며 AuthenticationManager를 생성하기 위해서는 AuthenticationManagerBuilder를 사용한다.
    // 로그인 처리 즉, 인증을 위해서는 UserDetailService를 통해서 필요한 정보들을 가져오는데, 여기서는 SignUpService에서 이를 처리한다.
    // SignUpService - UserDetailsService에서 @Override한 loadUserByUsername메소드를 통해 DB와 연결하여 사용자를 조회한다.
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(signUpService) // 로그인 할때 DB에 있는 사용자 계정을 조회할때 사용할 서비스
                                                   // 인증 방식 :  Security --> SignUpService
                                                   // 인증 저장소 : Security --> DB
                .passwordEncoder(passwordEncoder()); // 암호화된 비밀번호 비교
    }
}
