package nhn.rookie.hama.test2.repository;

import nhn.rookie.hama.test2.entity.Memo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
public class MemoRepositoryTests {

    @Autowired
    MemoRepository memoRepository;

    // MemoRepository 인터페이스 타입의 실제 객체가 어떤 것인지 확인
    @Test
    public void testClass() {
        System.out.println(memoRepository.getClass().getName()); // 스프링이 AOP 기능으로 클래스 자동생성. 동적 프록시 방식으로 클래스가 생성됨
    }

    // 100개의 새로운 Memo 객체를 생성하고 insert
    @Test
    public void testInsertDummies() {

        IntStream.rangeClosed(1,100).forEach(i -> {
            Memo memo = Memo.builder().memoText("Sample..."+i).build();
            memoRepository.save(memo);
        });
    }

    // findById()를 이용한 조회
    @Test
    public void testSelect() {

        // 데이터베이스에 존재하는 mno
        Long mno = 100L;

        Optional<Memo> result = memoRepository.findById(mno); // 이 순간에 SQL 처리

        System.out.println("=============================================");

        if (result.isPresent()) {
            Memo memo = result.get();
            System.out.println(memo);
        }

    }

    // getOne()을 이용한 조회
    @Transactional // 트랜잭션 처리를 위해 사용. getOne()을 사용하기 위해 필요.
    @Test
    public void testSelect2() {

        // 데이터베이스에 존재하는 mno
        Long mno = 100L;

        Memo memo = memoRepository.getOne(mno);

        System.out.println("=============================================");

        System.out.println(memo); // 실제 객체가 필요한 순간에 SQL 실행
    }

    // 수정
    @Test
    public void testUpdate() {

        Memo memo = Memo.builder().mno(100L).memoText("Update Text").build();

        System.out.println(memoRepository.save(memo));
    }

    // 삭제
    @Test
    public void testDelete() {

        Long mno = 100L;

        memoRepository.deleteById(mno); // return 타입은 null. 해당 데이터가 존재하지 않으면 예외 발생

    }

    // 페이징 처리
    @Test
    public void testPageDefault() {

        // 1페이지 10개
        Pageable pageable = PageRequest.of(0,10);

        // 첫 번째 쿼리에서는 limit 구문으로 페이징 처리. 두 번째 쿼리에서는 count()를 이용해 전체 개수 처리(이 쿼리는 데이터의 수가 충분하지 않다면 실행하지 않음)
        // findAll()에 Pageable 타입의 파라미터를 전달하면 페이징 처리에 관련된 쿼리들을 실행하고, 그 결과들을 이용해 Page<엔티티 타입> 객체로 저장
        Page<Memo> result = memoRepository.findAll(pageable);

        System.out.println(result); // 출력결과 : Page 1 of 10 containing nhn.rookie.hama.test2.entity.Memo instances
                                    // (10개의 페이지 중 첫 번째 페이지. 사이즈가 20이었다면 5개의 페이지 중 첫 번째.)

        System.out.println("------------------------------------------------");

        System.out.println("Total Pages: "+result.getTotalPages()); // 총 몇 페이지

        System.out.println("Total Count: "+result.getTotalElements()); // 전체 개수

        System.out.println("Page Number: "+result.getNumber()); // 현재 페이지 번호 0부터 시작

        System.out.println("Page Size: "+result.getSize()); // 페이지당 데이터 개수

        System.out.println("has next page?: "+result.hasNext()); // 다음 페이지 존재 여부

        System.out.println("first page?: "+result.isFirst()); // 시작 페이지(0) 여부

        System.out.println("------------------------------------------------");

        // 실제 페이지의 데이터 처리
        for (Memo memo : result.getContent()) {
            System.out.println(memo);
        }
    }

    // 정렬 조건을 추가한 페이징 처리
    @Test
    public void testSort() {

        Sort sort1 = Sort.by("mno").descending(); // mno 필드 값을 이용해서 역순으로 정렬 -> 쿼리에 order by 절이 추가됨

        Pageable pageable = PageRequest.of(0,10,sort1);

        Page<Memo> result = memoRepository.findAll(pageable);

        result.get().forEach(memo -> {
            System.out.println(memo);
        });

        System.out.println("------------------------------------------------");

        Sort sort2 = Sort.by("memoText").ascending();
        Sort sortAll = sort1.and(sort2); // and를 이용한 연결

        pageable = PageRequest.of(0,10,sortAll); // 결합된 정렬 조건 사용. 쿼리에서 정렬 조건이 추가됨

        result = memoRepository.findAll(pageable);

        result.get().forEach(memo -> {
            System.out.println(memo);
        });
    }

    // 쿼리 메서드를 이용해서 between, order by 구문 적용
    @Test
    public void testQueryMethods() {

        List<Memo> list = memoRepository.findByMnoBetweenOrderByMnoDesc(70L, 80L);

        for (Memo memo : list) {
            System.out.println(memo);
        }
    }

    // 쿼리 메서드와 Pageable을 결합하여 between, order by 구문 적용
    @Test
    public void testQueryMethodWithPageable() {

        Pageable pageable = PageRequest.of(0,10,Sort.by("mno").descending());

        Page<Memo> result = memoRepository.findByMnoBetween(10L,50L,pageable); // 10부터 50사이를 역순으로 10개씩 페이징 한 것의 첫 번째 페이지를 결과로 가져옴.

//        System.out.println(result); // 출력결과 : Page 1 of 5 containing nhn.rookie.hama.test2.entity.Memo instances

        result.get().forEach(memo -> System.out.println(memo));
    }

    // deleteBy로 데이터를 삭제
    @Commit         // 최종 결과를 커밋하기 위해 사용. 이를 적용하지 않으면 테스트 코드의 deleteBy는 기본적으로 롤백 처리됨.
    @Transactional  // deleteBy의 경우 select문으로 해당 엔티티 객체들을 가져오고 각 엔티티를 삭제하는 작업이 같이 이루어지기 때문에 필요
    @Test
    public void testDeleteQueryMethods() {

        // deleteBy는 실제 개발에서 많이 사용되지는 않는다. -> SQL처럼 한 번에 삭제가 이루어지는 것이 아니라 각 엔티티 객체를 하나씩 삭제하기 때문
        // 해당 테스트 코드는 9번의 delete 쿼리가 실행됨.
        memoRepository.deleteMemoByMnoLessThan(10L);
    }

    // @Query를 이용해 mno의 역순으로 정렬
    @Test
    public void testQueryAnnotation1() {

        List<Memo> list = memoRepository.getListDesc();

        for (Memo memo : list) {
            System.out.println(memo);
        }
    }

    // @Query 파라미터 바인딩(파라미터 이름을 활용하는 방식)으로 메모 내용 수정
    @Test
    public void testQueryAnnotation2() {

        memoRepository.updateMemoText(99L, "@Query 파라미터 바인딩(파라미터 이름)");
    }

    // @Query 파라미터 바인딩(자바 빈 스타일을 이용하는 방식)으로 메모 내용 수정
    @Test
    public void testQueryAnnotation3() {

        Memo memo = Memo.builder().mno(99L).memoText("@Query 파라미터 바인딩(자바 빈 객체)").build();
        memoRepository.updateMemoText(memo);
    }

    // @Query 페이징 처리
    @Test
    public void testQueryAnnotation4() {

        Pageable pageable = PageRequest.of(0,10,Sort.by("mno").descending());

        Page<Memo> result = memoRepository.getListWithQuery(80L, pageable); // countQuery 수행됨

        result.get().forEach(memo -> System.out.println(memo)); // mno: 99~90 결과 출력

        System.out.println("----------------------------------------");

        result = memoRepository.getListWithQuery(90L, pageable); // 데이터의 수가 모자라 countQuery 수행 안 됨

        result.get().forEach(memo -> System.out.println(memo)); // mno: 99~91 결과 출력
    }

    // Object[]를 사용하여 CURRENT_DATE도 함께 얻어 올 수 있다.
    @Test
    public void testQueryAnnotation5() {

        Pageable pageable = PageRequest.of(0,10,Sort.by("mno").descending());

        Page<Object[]> result = memoRepository.getListWithQueryObject(95L, pageable);

        result.get().forEach(element -> {
            for (Object o : element) {
                System.out.println(o);
            }
        });
    }

    // Native SQL 처리
    @Test
    public void testQueryAnnotation6() {

        List<Object[]> list = memoRepository.getNativeResult();

        for (Object[] objects : list) {
            for (Object object : objects) {
                System.out.print(object + " ");
            }
            System.out.println();
        }
    }

}
