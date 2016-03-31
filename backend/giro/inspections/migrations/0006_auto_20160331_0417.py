# -*- coding: utf-8 -*-
# Generated by Django 1.9.2 on 2016-03-31 04:17
from __future__ import unicode_literals

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('inspections', '0005_auto_20160302_0403'),
    ]

    operations = [
        migrations.AlterField(
            model_name='hotspot',
            name='inspection_image',
            field=models.OneToOneField(on_delete=django.db.models.deletion.CASCADE, related_name='hotspot', to='inspections.InspectionImage'),
        ),
        migrations.AlterField(
            model_name='icedam',
            name='inspection_image',
            field=models.OneToOneField(on_delete=django.db.models.deletion.CASCADE, related_name='icedam', to='inspections.InspectionImage'),
        ),
        migrations.AlterField(
            model_name='inspectionimage',
            name='path',
            field=models.TextField(default='', max_length=128),
        ),
    ]
