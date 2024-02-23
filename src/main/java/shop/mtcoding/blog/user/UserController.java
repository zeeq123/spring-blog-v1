package shop.mtcoding.blog.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import shop.mtcoding.blog._core.util.ApiUtil;
import shop.mtcoding.blog._core.util.Script;


@RequiredArgsConstructor // final이 붙은 애들에 대한 생성자를 만들어줌
@Controller
public class UserController {

    // 자바는 final 변수는 반드시 초기화가 되어야함.
    private final UserRepository userRepository;
    private final HttpSession session;

    // 왜 조회인데 post임? 민간한 정보는 body로 보낸다.
    // 로그인만 예외로 select인데 post 사용
    // select * from user_tb where username=? and password=?

    @GetMapping("/api/username-same-check")
    public @ResponseBody ApiUtil<?> usernameSameCheck(String username){
        User user = userRepository.findByUsername(username);
        if (user == null){ // 회원가입 가능
             return new ApiUtil<>(true);
        }else{ // 회원가입 불가능
            return new ApiUtil<>(false);
        }
    }

    @PostMapping("/login")
    public String login(UserRequest.LoginDTO requestDTO){


        System.out.println(requestDTO); // toString -> @Data

        if(requestDTO.getUsername().length() < 3){
            throw new RuntimeException("유저네임 길이가 너무 짧아요.");
        }

        User user = userRepository.findByUsername(requestDTO.getUsername());

        if (!BCrypt.checkpw(requestDTO.getPassword(), user.getPassword())){
            throw new RuntimeException("패스워드가 틀렸습니다.");
        }
        session.setAttribute("sessionUser", user); // 락카에 담음 (StateFul)

        return "redirect:/"; // 컨트롤러가 존재하면 무조건 redirect 외우기
    }

    @PostMapping("/join")
    public String join(UserRequest.JoinDTO requestDTO){
        System.out.println(requestDTO);

        String rawPassword = requestDTO.getPassword();
        String encPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        requestDTO.setPassword(encPassword);

        userRepository.save(requestDTO); // 모델에 위임하기

        return "redirect:/loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "user/joinForm";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }

    @GetMapping("/user/updateForm")
    public String updateForm() {
        return "user/updateForm";
    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/";
    }
}
