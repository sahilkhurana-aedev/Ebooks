declare @id int
DECLARE @Random INT
declare @NumberOfRecords int = 100;
DECLARE @FromDate DATETIME2(0)
DECLARE @ToDate   DATETIME2(0)
SET @FromDate = '2020-03-28 00:00:00'
SET @ToDate = '2020-03-28 23:59:59'
declare @dateValue DATETIME2(0);
select @id = 1
while @id >=1 and @id <= @NumberOfRecords
begin
	select @Random = ROUND((((DATEDIFF(SECOND, @FromDate, @ToDate))-1) * RAND()), 0)
	insert into event_data_src (empID, FirstName, LastName, BdgId, ReaderName, EventMsg, EventDate) 
		values(@id, 'Richard' + convert(varchar(5), @id), 'Daniel' + convert(varchar(5), @id), CAST(RAND() * 1000000 AS varchar), 'CCURE', 'Badge In', DATEADD(SECOND, @Random, @FromDate));
	select @id = @id + 1
end