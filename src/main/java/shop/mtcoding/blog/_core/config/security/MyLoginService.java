package shop.mtcoding.blog._core.config.security;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import shop.mtcoding.blog.user.User;
import shop.mtcoding.blog.user.UserRepository;

// POST, /login, x-www-form-urlencoded, 키값이 username, password
@RequiredArgsConstructor
@Service
public class MyLoginService implements UserDetailsService {

    private final UserRepository userRepository;
    private final HttpSession session;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("loadUserByUsername : " + username);
        User user = userRepository.findByUsername(username);

        if(user == null){
            System.out.println("user는 null");
            return null;
        }else{
            System.out.println("user 찾음");
            session.setAttribute("sessionUser", user);
            return new MyLoginUser(user); // SecurityContextHolder 저장
        }
    }
}
