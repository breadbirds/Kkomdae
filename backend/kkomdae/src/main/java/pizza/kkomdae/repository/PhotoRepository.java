package pizza.kkomdae.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Photo;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo,Long> {
    List<Photo> getPhotosByLaptopTestResult(LaptopTestResult laptopTestResult);

    Photo findByLaptopTestResultAndPhotoId(LaptopTestResult laptopTestResult, long photoId);

    Optional<Photo> findByLaptopTestResultAndType(LaptopTestResult test, int type);
}
