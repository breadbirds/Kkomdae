package pizza.kkomdae.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pizza.kkomdae.dto.PdfInfo;
import pizza.kkomdae.entity.LaptopTestResult;
import pizza.kkomdae.entity.Photo;
import pizza.kkomdae.repository.laptopresult.LapTopTestResultRepository;
import pizza.kkomdae.repository.rent.RentRepository;
import pizza.kkomdae.s3.S3Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class PdfService {
    private final S3Service s3Service;
    private final LapTopTestResultRepository lapTopTestResultRepository;
    private final RentRepository rentRepository;

    public PdfService(S3Service s3Service, LapTopTestResultRepository lapTopTestResultRepository, RentRepository rentRepository) {
        this.s3Service = s3Service;
        this.lapTopTestResultRepository = lapTopTestResultRepository;
        this.rentRepository = rentRepository;
    }

    public String makeAndUploadPdf(long testId) {
        LaptopTestResult result = lapTopTestResultRepository.findByIdWithStudentAndDeviceAndPhotos(testId);
        LaptopTestResult rent = null;
        List<Photo> rentPhotos = null;

        for (Photo photo : result.getPhotos()) {
            result.setSumOfDamages(result.getSumOfDamages()+photo.getDamage());
        }
        if (result.getRelease()) {
            rent = result.getRent().getLaptopTestResults().get(0);
            rentPhotos = rent.getPhotos();
        }
        String fileName;
        try {
            PdfInfo pdfInfo = new PdfInfo(result, rent, rentPhotos);
            ByteArrayOutputStream baso = initPdf(pdfInfo);
            fileName = s3Service.uploadPdf(baso, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        result.setPdfFileName(fileName);
        result.setStage(6);
        if (result.getRelease()) result.getDevice().setRelease(true);
        lapTopTestResultRepository.save(result);
        return fileName;
    }

    private ByteArrayOutputStream initPdf(PdfInfo info) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);

        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        // 한글 폰트
        PdfFont koreanFont = PdfFontFactory.createFont("/NanumGothic-Regular.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        PdfFont koreanFontBold = PdfFontFactory.createFont("/NanumGothic-Bold.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);

        // 빨간색 정의
        DeviceRgb redColor = new DeviceRgb(255, 0, 0);

        // 제목 추가 (밑줄 추가)
        Paragraph title = new Paragraph("삼성 청년 S/W 아카데미 노트북 수령 확인서")
                .setFont(koreanFontBold)
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setUnderline();
        document.add(title);
        document.add(new

                Paragraph("\n"));

        // 노트북 수령 규정 섹션
        addRegulations(document, koreanFont, redColor);
        document.add(new

                Paragraph("\n"));

        // 노트북 구성품 테이블
        addComponentsTable(document, koreanFont, info);
        document.add(new

                Paragraph("\n"));

        // 서명 섹션
        addSignatureSection(document, koreanFont, info);
        document.add(new

                Paragraph("\n\n"));

        // 노트북 촬영 섹션
        addPhotoSection(document, koreanFont, info.getPhotos(),"촬영");

        if (info.isRelease()) addPhotoSection(document, koreanFont, info.getRentPhotos(),"분석");
        document.close();
        return baos;
    }

    private void addRegulations(Document document, PdfFont font, DeviceRgb redColor) {
        // 노트북 수령 규정 목록
        String regulation = "1. 분실 또는 도난 당하였을 경우 동일한 성능의 노트북으로 변상한다.";
        String regulation1 = "※ 노트북 기종: NT850XCJ-XB72B(10세대) 단가: 2,770,000원\n※ 노트북 기종: NT761XDA-X07/C(11세대) 단가: 2,452,000원\n※ 노트북 기종: NT961XFH-X01/C(13세대) 단가: 2,682,000원\n※ 노트북 기종: NT961XGL-COM(14세대) 단가: 2,999,655원";
        // 특별한 색상 처리가 필요한 규정 (2번과 9번 빨간색)
        String regulation2 = "2. 노트북을 파손하였을 경우 전액 수령자 비용부담으로 수리하여 원 상태로 반납하여야 한다.\n   실금, 흠집 등 사용상의 문제가 없는 미세 하자 역시 파손에 포함되므로 최초 수령 시 외관 상태 촬영 必";

        List<String> regularRegulations = Arrays.asList(
                "3. 노트북을 임의로 타인에게 양도 및 대여할 수 없으며, 이로 인해 발생하는 모든 문제에 대해\n   수령자의 책임으로 한다.",
                "4. 노트북에 임의로 설치한 소프트웨어 및 데이터의 저작권 위반 혹은 라이센스 문제 발생 시\n   수령자의 책임으로 한다.",
                "5. 노트북은 교육 외(게임, 쇼핑 등) 용도로 사용하지 않는다",
                "6. 지급 받은 노트북은 오프라인 출석 시 필수 지참하며, 교육 종료 시 사무국으로 원상태로\n   (구성품 포함) 반납한다.",
                "7. 교육 중 퇴소할 경우 노트북은(구성품 포함)을 이상 없이 반납한다.",
                "8. 허가된 장소(자택, 캠퍼스, 오프라인 강의실, 기타 일체의 허가 받은 장소)외 무단 반출할 경우\n   절도에 해당하는 민·형사상 법적 책임이 있으며, 위 내용 위반 시 즉시 중도 퇴소한다."
        );

        String regulation9 = "9. 아래 노트북(구성품 포함)이 이상 없음을 확인하며 수령 후 이상 여부가 확인된 경우 즉시 고지하고\n   확인 작업에 적극 협조한다.";

        // 일반 규정 추가
        Paragraph p0 = new Paragraph(regulation)
                .setFont(font)
                .setFontSize(10);
        document.add(p0);

        Paragraph p1 = new Paragraph(regulation1)
                .setFont(font)
                .setFontSize(9);
        document.add(p1);

        // 2번 규정 (빨간색)
        Paragraph p2 = new Paragraph(regulation2)
                .setFont(font)
                .setFontSize(10)
                .setFontColor(redColor);
        document.add(p2);

        // 일반 규정 추가 (3-8번)
        for (String normalRegulation : regularRegulations) {
            Paragraph p = new Paragraph(normalRegulation)
                    .setFont(font)
                    .setFontSize(10);
            document.add(p);
        }

        // 9번 규정 (빨간색)
        Paragraph p9 = new Paragraph(regulation9)
                .setFont(font)
                .setFontSize(10)
                .setFontColor(redColor);
        document.add(p9);
    }

    private void addComponentsTable(Document document, PdfFont font, PdfInfo info) {
        // 노트북 구성품 테이블 생성
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1, 2, 1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        // 헤더 셀 추가
        addHeaderCell(table, font, "구분", 1, 1);
        addHeaderCell(table, font, "수량", 1, 1);
        addHeaderCell(table, font, "수령", 1, 1);
        addHeaderCell(table, font, "반납", 1, 1);
        addHeaderCell(table, font, "구분", 1, 1);
        addHeaderCell(table, font, "수량", 1, 1);
        addHeaderCell(table, font, "수령", 1, 1);
        addHeaderCell(table, font, "반납", 1, 1);


        String rentLaptop = "";
        String rentMouse = "";
        String rentPowerCable = "";
        String rentBag = "";
        String rentAdapter = "";
        String rentMousepad = "";
        if (info.isRelease()) {
            rentLaptop = Integer.toString(info.getRentLaptopCount());
            rentMouse = Integer.toString(info.getRentMouseCount());
            rentPowerCable = Integer.toString(info.getRentPowerCableCount());
            rentBag = Integer.toString(info.getRentBagCount());
            rentAdapter = Integer.toString(info.getRentAdapterCount());
            rentMousepad = Integer.toString(info.getRentMousePadCount());
        }

        // 테이블 내용 - 첫 번째 행
        addComponentRow(table, font, "노트북", Integer.toString(info.getLaptopCount()), "마우스\n*리시버 포함", Integer.toString(info.getMouseCount()), info, rentLaptop, rentMouse);

        // 테이블 내용 - 두 번째 행
        addComponentRow(table, font, "전원선", Integer.toString(info.getPowerCableCount()), "가방\n*가방끈 포함", Integer.toString(info.getBagCount()), info, rentPowerCable, rentBag);

        // 테이블 내용 - 세 번째 행
        addComponentRow(table, font, "어댑터", Integer.toString(info.getAdapterCount()), "마우스패드", Integer.toString(info.getMousePadCount()), info, rentAdapter, rentMousepad);

        // 특이사항 행
        Cell specialCell = new Cell(1, 1)
                .add(new Paragraph("특이사항").setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
        table.addCell(specialCell);
        Cell descriptionCell = new Cell(1, 7)
                .add(new Paragraph(info.getDescription()).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
        table.addCell(descriptionCell);

        document.add(table);
    }

    private void addHeaderCell(Table table, PdfFont font, String text, int rowspan, int colspan) {
        Cell cell = new Cell(rowspan, colspan)
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
        table.addCell(cell);
    }

    private void addComponentRow(Table table, PdfFont font, String item1, String qty1, String item2, String qty2, PdfInfo info, String rentQty1, String rentQty2) {

        if (info.isRelease()) { // 반납
            // 첫 번째 항목
            Cell cell1 = new Cell()
                    .add(new Paragraph(item1).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell1);

            // 첫 번째 항목 수량
            Cell cell2 = new Cell()
                    .add(new Paragraph(String.valueOf(1)).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell2);

            // 첫 번째 항목 수령
            Cell cell3 = new Cell()
                    .add(new Paragraph(rentQty1).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell3);

            // 첫 번째 항목 반납
            Cell cell4 = new Cell()
                    .add(new Paragraph(qty1).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell4);

            // 두 번째 항목
            Cell cell5 = new Cell()
                    .add(new Paragraph(item2).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell5);

            // 두 번째 항목 수량
            Cell cell6 = new Cell()
                    .add(new Paragraph(String.valueOf(1)).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell6);

            // 두 번째 항목 수령
            Cell cell7 = new Cell()
                    .add(new Paragraph(rentQty2).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell7);

            // 두 번째 항목 반납
            Cell cell8 = new Cell()
                    .add(new Paragraph(qty2).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell8);
        } else { // 대여
            // 첫 번째 항목
            Cell cell1 = new Cell()
                    .add(new Paragraph(item1).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell1);

            // 첫 번째 항목 수량
            Cell cell2 = new Cell()
                    .add(new Paragraph(String.valueOf(1)).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell2);

            // 첫 번째 항목 수령
            Cell cell3 = new Cell()
                    .add(new Paragraph(qty1).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell3);

            // 첫 번째 항목 반납
            Cell cell4 = new Cell()
                    .add(new Paragraph("☐").setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell4);

            // 두 번째 항목
            Cell cell5 = new Cell()
                    .add(new Paragraph(item2).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell5);

            // 두 번째 항목 수량
            Cell cell6 = new Cell()
                    .add(new Paragraph(String.valueOf(1)).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell6);

            // 두 번째 항목 수령
            Cell cell7 = new Cell()
                    .add(new Paragraph(qty2).setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell7);

            // 두 번째 항목 반납
            Cell cell8 = new Cell()
                    .add(new Paragraph("☐").setFont(font).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(new SolidBorder(ColorConstants.BLACK, 1));
            table.addCell(cell8);
        }

    }

    private void addSignatureSection(Document document, PdfFont font, PdfInfo info) {
        // 서명 문구
        Paragraph signParagraph = new Paragraph("본인은 노트북을 지급 받아 사용하는 데에 따른 위 사항에 동의하고 준수할 것을 확인합니다.")
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(signParagraph);
        document.add(new Paragraph("\n"));

        // 서명 테이블
        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 3}));
        signatureTable.setWidth(UnitValue.createPercentValue(100));

        String rentDate;
        if (info.getRentDate() == null) {
            rentDate = "";
            log.info("rentdate : null");
        } else {
            log.info("rentdate : {}", info.getRentDate());
            rentDate = info.getRentDate().toString();
        }
        String returnDate;
        if (info.getReturnDate() == null) {
            log.info("returndate : null");
            returnDate = "";
        } else {
            log.info("returndate : {}", info.getReturnDate());
            returnDate = info.getReturnDate().toString();
        }
        // 첫 번째 행
        addSignatureRow(signatureTable, font, "수 령 일 자 :", rentDate, "반 납 일 자 :", returnDate);

        // 두 번째 행
        if (info.isRelease()) {
            addSignatureRow(signatureTable, font, "수 령 서 명 :", info.getName(), "반 납 서 명 :", info.getName());
        } else {
            addSignatureRow(signatureTable, font, "수 령 서 명 :", info.getName(), "반 납 서 명 :", "");
        }


        // 세 번째 행
        addSignatureRow(signatureTable, font, "시 리 얼 번 호 :", info.getSerial(), "성 명 :", info.getName());

        // 네 번째 행
        addSignatureRow(signatureTable, font, "바 코 드 번 호 :", info.getBarcode(), "이 메 일 :", info.getEmail());

        document.add(signatureTable);
    }

    private void addSignatureRow(Table table, PdfFont font, String label1, String value1, String label2, String value2) {
        // 첫 번째 라벨
        Cell labelCell1 = new Cell()
                .add(new Paragraph(label1).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER);
        table.addCell(labelCell1);

        // 첫 번째 값
        Cell valueCell1 = new Cell()
                .add(new Paragraph(value1).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER);
        table.addCell(valueCell1);

        // 두 번째 라벨
        Cell labelCell2 = new Cell()
                .add(new Paragraph(label2).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER);
        table.addCell(labelCell2);

        // 두 번째 값
        Cell valueCell2 = new Cell()
                .add(new Paragraph(value2).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.LEFT)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorder(Border.NO_BORDER);
        table.addCell(valueCell2);
    }

    private void addPhotoSection(Document document, PdfFont font, List<Photo> photos, String status) throws MalformedURLException {

        if (status.equals("분석")) {
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        }
        // 사진 섹션 제목 (밑줄 추가)
        Paragraph photoTitle = new Paragraph("삼성 청년 S/W 아카데미 노트북 상태 " + status)
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setUnderline();
        document.add(photoTitle);
        document.add(new Paragraph("\n"));


        // 첫 번째 페이지: 전면부와 후면부 사진
        addPhotoItem(document, font, "[전면부 사진]", s3Service.generatePresignedUrl(photos.get(0).getName()));
        addPhotoItem(document, font, "[후면부 사진]", s3Service.generatePresignedUrl(photos.get(1).getName()));

        // 두 번째 페이지 후 페이지 나누기
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        addPhotoItem(document, font, "[우측 사진]", s3Service.generatePresignedUrl(photos.get(2).getName()));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        addPhotoItem(document, font, "[좌측 사진]", s3Service.generatePresignedUrl(photos.get(3).getName()));
        // 세 번째 페이지 후 페이지 나누기
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        // 세 번째 페이지: 액정과 키판 사진
        addPhotoItem(document, font, "[액정 사진/카메라 랜즈 포함]", s3Service.generatePresignedUrl(photos.get(4).getName()));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        addPhotoItem(document, font, "[키판 사진]", s3Service.generatePresignedUrl(photos.get(5).getName()));


        // 기타 확인 사진을 위한 변수 n
        /*int n = 5; // 여기서 필요한 기타 사진 개수 설정

        // 기타 확인 사진 추가 (n개)
        if (n > 0) {
            // 기타 사진 시작 전 페이지 나누기
            document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            for (int i = 0; i < n; i++) {
                // 페이지당 2개씩 표시
                if (i > 0 && i % 2 == 0) {
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }

                addPhotoItem(document, font, "[기타 확인이 필요하다고 판단되는 사진]");
            }
        }*/
    }

    // 사진 항목 하나를 추가하는 헬퍼 메소드
    private void addPhotoItem(Document document, PdfFont font, String description, String url) throws MalformedURLException {
        // 제목을 Paragraph로 추가 (왼쪽 정렬)
        Paragraph descParagraph = new Paragraph(description)
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.LEFT)
                .setPaddingBottom(3);
        document.add(descParagraph);

        // 사진만 포함하는 1x1 테이블 생성
        Table photoTable = new Table(UnitValue.createPercentArray(new float[]{1}));
        photoTable.setWidth(UnitValue.createPercentValue(100));

        // 사진 셀 추가
        Cell photoCell = new Cell()
                .setHeight(266) // 모든 사진 셀의 높이 동일하게 설정
                .setBorder(new SolidBorder(ColorConstants.BLACK, 2))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // 이미지 추가


        Image image = new Image(ImageDataFactory.create(new URL(url)));

        // 이미지 크기와 정렬 설정
        image.setHeight(260);
        image.setTextAlignment(TextAlignment.CENTER);
        image.setHorizontalAlignment(HorizontalAlignment.CENTER);
        image.setBorder(new SolidBorder(ColorConstants.BLACK, 1));

        photoCell.add(image);
        photoTable.addCell(photoCell);

        // 문서에 테이블 추가
        document.add(photoTable);
    }


}