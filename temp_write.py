content = """name: CI > temp_write.py
        if: always(
        uses: EnricoMi/publish-unit-test-result-action@v2 
        with: 
            **/test-results/**/*.xml 
open('.github/workflows/blank.yml', 'w').write(content) 
