package nhn.rookie.hama.test2.repository;

import nhn.rookie.hama.test2.entity.Memo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {

    List<Memo> findByMnoBetweenOrderByMnoDesc(Long from, Long to); // mno를 기준으로 between + order by

    // Pageable 파라미터는 모든 쿼리 메서드에 적용할 수 있으므로 일반적인 경우에는 쿼리 메서드에 정렬 조건 생략
    Page<Memo> findByMnoBetween(Long from, Long to, Pageable pageable); // mno를 기준으로 between, Pageable을 통한 정렬

    void deleteMemoByMnoLessThan(Long num);

    ///////////////////////////////////////////////////////
    // @Query 어노테이션
    @Query("select m from Memo m order by m.mno desc")
    List<Memo> getListDesc();

    // DML을 처리하는 기능은 @Modifying과 함께 사용
    // 파라미터 이름 활용
    @Transactional
    @Modifying
    @Query("update Memo m set m.memoText = :memoText where m.mno = :mno")
    int updateMemoText(@Param("mno") Long mno, @Param("memoText") String memoText);

    // 자바 빈 스타일 이용 (객체를 사용)
    @Transactional
    @Modifying
    @Query("update Memo m set m.memoText = :#{#param.memoText} where m.mno = :#{#param.mno}")
    int updateMemoText(@Param("param") Memo memo);

    // 리턴 타입을 Page<엔티티타입>으로 지정하는 경우에는 count를 계산할 수 있는 쿼리가 필수! 별도의 countQuery라는 속성을 적용해 주고 Pageable 타입의 파라미터 전달
    @Query(value = "select m from Memo m where m.mno > :mno", countQuery = "select count(m) from Memo m where m.mno > :mno")
    Page<Memo> getListWithQuery(Long mno, Pageable pageable);

    // 적당한 엔티티 타입이 존재하지 않는 경우 Object[] 타입을 리턴 타입으로 지정할 수 있다.
    @Query(value = "select m.mno, m.memoText, CURRENT_DATE from Memo m where m.mno > :mno",
                    countQuery = "select count(m) from Memo m where m.mno > :mno")
    Page<Object[]> getListWithQueryObject(Long mno, Pageable pageable);

    // SQL 구문을 그대로 활용할 수 있다. (복잡한 JOIN 구문 등을 처리할 때 주로 사용) 객체명이 아니라 지정해 준 데이터베이스의 이름들응 사용하는 것 유의하자!
    @Query(value = "select * from tbl_memo where mno > 0", nativeQuery = true)
    List<Object[]> getNativeResult();
}
