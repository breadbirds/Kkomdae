from django.shortcuts import render

# Create your views here.
# s3app/views.py
import boto3
from django.http import JsonResponse
from django.conf import settings
from django.views.decorators.csrf import csrf_exempt
             
def presigned_url_view(request):
    """
    ?file=파일명 으로 넘어온 파라미터에 대해
    AWS S3의 사전 서명(Presigned) URL을 생성하여 반환한다.
    """
    filename = request.GET.get('file')
    if not filename:
        return JsonResponse({'error': 'No filename provided'}, status=400)

    # boto3 s3 클라이언트 생성
    s3_client = boto3.client(
        's3',
        aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
        aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
        region_name=settings.AWS_REGION
    )

    # 실제로 presigned url 생성
    try:
        key = f"{settings.S3_PREFIX}{filename}"
        url = s3_client.generate_presigned_url(
            ClientMethod='get_object',
            Params={
                'Bucket': settings.AWS_S3_BUCKET_NAME,
                'Key': key
            },
            ExpiresIn=600  # 600초(10분) 동안만 유효
        )
        return JsonResponse({'url': url})
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)

@csrf_exempt
def upload_battery_report_view(request):
    """
    윈도우 앱(클라이언트)에서
    배터리 리포트 파일을 POST로 업로드하면,
    Django가 해당 파일을 S3에 업로드하는 예시 뷰.
    """
    if request.method == 'POST':
        # 1) POST 요청에서 파일 꺼내오기
        #    <input type="file" name="file"> 와 동일한 key 이름 "file"
        battery_file = request.FILES.get('file')
        if not battery_file:
            return JsonResponse({'error': 'No file uploaded'}, status=400)

        # 2) boto3 클라이언트 생성 (settings.py 에 AWS 관련 설정이 있다고 가정)
        s3_client = boto3.client(
            's3',
            aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
            aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
            region_name=settings.AWS_REGION
        )

        # 3) S3 키 이름 결정
        #    예: S3_PREFIX가 "reports/" 라면
        filename = battery_file.name  # 예: battery_report_Unknown_20250328_103045.html
        key = f"{settings.S3_PREFIX}{filename}"

        # 4) s3.upload_fileobj() 로 업로드
        try:
            s3_client.upload_fileobj(battery_file, settings.AWS_S3_BUCKET_NAME, key)
            return JsonResponse({
                'message': 'Upload success',
                'file': filename,
                's3_key': key
            })
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

    return JsonResponse({'error': 'Invalid method'}, status=405)