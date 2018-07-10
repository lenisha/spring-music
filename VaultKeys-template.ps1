# Create a column master key in Azure Key Vault and encrypt the column

# Azure subscription and service princial settings 
$applicationId = "<Service principal app id>";
$secret = "<service principal secret>"
$tenantId = "<tenant Id>";
$SubscriptionId = "<subscription id>"

# Azure Key Vault settings
$akvName = "springvault"
$akvKeyName = "PoshKey2"

# Connect to your database (Azure SQL database).
$serverName = "<>.database.windows.net"
$databaseName = "<db name>"
$dbpassword = "<db admin password>"
$dbuser = "<db user>"
$sqlConnectionString = "Data Source=$servername;Initial Catalog=$databasename;User ID=$dbuser;Password=$dbpassword;MultipleActiveResultSets=False;Connect Timeout=30;Encrypt=True;TrustServerCertificate=False;Column Encryption Setting=Enabled"

# Column Encryption Key and Master key Name in the DB
$cekName = "CEKPosh2"
$cmkName = "CMKPosh2"

# Login into Azure Subsription
$securePassword = $secret | ConvertTo-SecureString -AsPlainText -Force
$credential = New-Object -TypeName System.Management.Automation.PSCredential -ArgumentList $applicationId, $securePassword
Connect-AzureRmAccount -ServicePrincipal  -Credential $credential -TenantId $tenantId

$azureCtx = Set-AzureRMConteXt -SubscriptionId $SubscriptionId # Sets the context for the below cmdlets to the specified subscription.

# Create a key in the Vault
$akvKey = Add-AzureKeyVaultKey -VaultName $akvName -Name $akvKeyName -Destination "Software"

# Import the SqlServer module.
#  If needed install it > Install-Module -Name SqlServer -Scope CurrentUser
Import-Module SqlServer


# connect to database
$database = Get-SqlDatabase -ConnectionString $sqlConnectionString

# Create a SqlColumnMasterKeySettings object for your column master key. 
$cmkSettings = New-SqlAzureKeyVaultColumnMasterKeySettings -KeyURL $akvKey.ID 

# Create column master key metadata in the database.
New-SqlColumnMasterKey -Name $cmkName -InputObject $database -ColumnMasterKeySettings $cmkSettings

# Authenticate to Azure (in this example using same credential as login)
#   * Enter a Client ID, Secret, and Tenant ID:
Add-SqlAzureAuthenticationContext -ClientID $applicationId -Secret $secret -Tenant $tenantId


# Generate a column encryption key, encrypt it with the column master key and create column encryption key metadata in the database. 
New-SqlColumnEncryptionKey -Name $cekName -InputObject $database -ColumnMasterKey $cmkName

# Change encryption schema
$encryptionChanges = @()

# Add changes for table [dbo].[artist]
$encryptionChanges += New-SqlColumnEncryptionSettings -ColumnName dbo.album.artist -EncryptionType Deterministic -EncryptionKey $cekName
Set-SqlColumnEncryption -ColumnEncryptionSettings $encryptionChanges -InputObject $database
