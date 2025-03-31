from django.urls import path
from . import views

urlpatterns = [
    # AWS S3 presigned URL을 생성하는 URL
    path('presigned-url/', views.presigned_url_view, name='presigned_url'),
    # 배터리 리포트 파일 업로드를 위한 URL
    path('upload_battery/', views.upload_battery_report_view, name='upload_battery_report'),
]
