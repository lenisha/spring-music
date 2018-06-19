/****** Script for SelectTopNRows command from SSMS  ******/
SELECT TOP (1000) [id]
      ,[album_id]
      ,[artist]
      ,[genre]
      ,[release_year]
      ,[title]
      ,[track_count]
  FROM [dbo].[album]

DECLARE @YEAR VARCHAR(255)  = '1969'
SELECT * FROM [dbo].[album]
WHERE [release_year] = @YEAR