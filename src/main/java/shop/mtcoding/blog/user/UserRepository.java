package shop.mtcoding.blog.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository // IoC에 new하는 방법
public class UserRepository {

    // DB에 접근할 수 있는 매니저 객체
    // 스프링이 만들어서 IoC에 넣어둔다.
    // DI에서 꺼내 쓰기만 하면된다.
    private EntityManager em;

    // 생성자 주입 (DI 코드)
    public UserRepository(EntityManager em) {
        this.em = em;
    }



    @Transactional // db에 write 할때는 필수
    public void save(UserRequest.JoinDTO requestDTO){
        Query query = em.createNativeQuery("insert into user_tb(username, password, email, created_at) values(?,?,?, now())");
        query.setParameter(1, requestDTO.getUsername());
        query.setParameter(2, requestDTO.getPassword());
        query.setParameter(3, requestDTO.getEmail());
        query.executeUpdate();
    }

public User findByUsernameAndPassword(UserRequest.LoginDTO requestDTO) {
    Query query = em.createNativeQuery("select * from user_tb where username=? and password=?", User.class);
    query.setParameter(1, requestDTO.getUsername());
    query.setParameter(2, requestDTO.getPassword());

    try{
        User user = (User) query.getSingleResult();
        return user;
    } catch(Exception e){
        return null;
    }
}

    public User findUserById(int id) {
        Query query = em.createNativeQuery("select * from user_tb where id=?", User.class);
        query.setParameter(1, id);

        User user = (User) query.getSingleResult();

        return user;
    }

    @Transactional
    public void update(UserRequest.UpdateDTO requestDTO, int id) {
        Query query = em.createNativeQuery("update user_tb set password=? where id=?");
        query.setParameter(1,requestDTO.getPassword());
        query.setParameter(2,id);
        query.executeUpdate();
    }


    public User findByUsername(String username) {
        Query query = em.createNativeQuery("select * from user_tb where username=?", User.class);
        query.setParameter(1, username);

        try{
            User user = (User) query.getSingleResult();
            return user;
        } catch(Exception e){
            return null;
        }
    }
}
